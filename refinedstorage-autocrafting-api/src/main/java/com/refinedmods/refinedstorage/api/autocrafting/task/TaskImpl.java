package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskImpl implements Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskImpl.class);

    private final TaskId id = TaskId.create();
    private final ResourceKey resource;
    private final long amount;
    private final long startTime = System.currentTimeMillis();
    private final Map<Pattern, AbstractTaskPattern> patterns;
    private final List<AbstractTaskPattern> completedPatterns = new ArrayList<>();
    private final MutableResourceList initialRequirements = MutableResourceListImpl.create();
    private final MutableResourceList internalStorage;
    private TaskState state = TaskState.READY;

    TaskImpl(final TaskPlan plan, final MutableResourceList internalStorage) {
        this.internalStorage = internalStorage;
        this.resource = plan.resource();
        this.amount = plan.amount();
        this.patterns = plan.patterns().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> createTaskPattern(e.getKey(), e.getValue()),
            (a, b) -> a,
            LinkedHashMap::new
        ));
        plan.initialRequirements().forEach(initialRequirements::add);
    }

    public static Task fromPlan(final TaskPlan plan) {
        return new TaskImpl(plan, MutableResourceListImpl.create());
    }

    private static AbstractTaskPattern createTaskPattern(final Pattern pattern,
                                                         final TaskPlan.PatternPlan patternPlan) {
        return switch (pattern.type()) {
            case INTERNAL -> new InternalTaskPattern(pattern, patternPlan);
            case EXTERNAL -> new ExternalTaskPattern(pattern, patternPlan);
        };
    }

    @Override
    public TaskId getId() {
        return id;
    }

    @Override
    public TaskState getState() {
        return state;
    }

    private void updateState(final TaskState newState) {
        LOGGER.debug("Task {} state changed from {} to {}", id.id(), state, newState);
        this.state = newState;
    }

    @Override
    public boolean step(final RootStorage rootStorage,
                        final ExternalPatternInputSink externalPatternInputSink,
                        final StepBehavior stepBehavior) {
        return switch (state) {
            case READY -> startTask(rootStorage);
            case EXTRACTING_INITIAL_RESOURCES -> extractInitialResourcesAndTryStartRunningTask(rootStorage);
            case RUNNING -> stepPatterns(rootStorage, externalPatternInputSink, stepBehavior);
            case RETURNING_INTERNAL_STORAGE -> returnInternalStorageAndTryCompleteTask(rootStorage);
            case COMPLETED -> false;
        };
    }

    @Override
    public void cancel() {
        state = TaskState.RETURNING_INTERNAL_STORAGE;
    }

    @Override
    public TaskStatus getStatus() {
        final TaskStatusBuilder builder = new TaskStatusBuilder(id, resource, amount, startTime);
        double totalWeightedCompleted = 0;
        double totalWeight = 0;
        for (final AbstractTaskPattern pattern : patterns.values()) {
            pattern.appendStatus(builder);
            totalWeightedCompleted += pattern.getPercentageCompleted() * pattern.getWeight();
            totalWeight += pattern.getWeight();
        }
        for (final AbstractTaskPattern pattern : completedPatterns) {
            totalWeightedCompleted += pattern.getWeight();
            totalWeight += pattern.getWeight();
        }
        internalStorage.getAll().forEach(
            internalResource -> builder.stored(internalResource, internalStorage.get(internalResource))
        );
        return builder.build(totalWeight == 0 ? 0 : totalWeightedCompleted / totalWeight);
    }

    private boolean startTask(final RootStorage rootStorage) {
        updateState(TaskState.EXTRACTING_INITIAL_RESOURCES);
        return extractInitialResourcesAndTryStartRunningTask(rootStorage);
    }

    private boolean extractInitialResourcesAndTryStartRunningTask(final RootStorage rootStorage) {
        boolean extractedAll = true;
        boolean extractedAny = false;
        final Set<ResourceKey> initialRequirementResources = new HashSet<>(initialRequirements.getAll());
        for (final ResourceKey initialRequirementResource : initialRequirementResources) {
            final long needed = initialRequirements.get(initialRequirementResource);
            final long extracted = rootStorage.extract(initialRequirementResource, needed, Action.EXECUTE, Actor.EMPTY);
            if (extracted > 0) {
                extractedAny = true;
            }
            LOGGER.debug("Extracted {}x {} from storage", extracted, initialRequirementResource);
            if (extracted != needed) {
                extractedAll = false;
            }
            if (extracted > 0) {
                initialRequirements.remove(initialRequirementResource, extracted);
                internalStorage.add(initialRequirementResource, extracted);
            }
        }
        if (extractedAll) {
            updateState(TaskState.RUNNING);
        }
        return extractedAny;
    }

    private boolean stepPatterns(final RootStorage rootStorage,
                                 final ExternalPatternInputSink externalPatternInputSink,
                                 final StepBehavior stepBehavior) {
        final var it = patterns.entrySet().iterator();
        boolean changed = false;
        while (it.hasNext()) {
            final var pattern = it.next();
            final PatternStepResult result = stepPattern(rootStorage, externalPatternInputSink, stepBehavior, pattern);
            if (result == PatternStepResult.COMPLETED) {
                it.remove();
            }
            changed |= result.isChanged();
        }
        if (patterns.isEmpty()) {
            if (internalStorage.isEmpty()) {
                updateState(TaskState.COMPLETED);
            } else {
                updateState(TaskState.RETURNING_INTERNAL_STORAGE);
            }
        }
        return changed;
    }

    private PatternStepResult stepPattern(final RootStorage rootStorage,
                                          final ExternalPatternInputSink externalPatternInputSink,
                                          final StepBehavior stepBehavior,
                                          final Map.Entry<Pattern, AbstractTaskPattern> pattern) {
        PatternStepResult result = PatternStepResult.IDLE;
        if (!stepBehavior.canStep(pattern.getKey())) {
            return result;
        }
        final int steps = stepBehavior.getSteps(pattern.getKey());
        for (int i = 0; i < steps; ++i) {
            final PatternStepResult stepResult = pattern.getValue().step(
                internalStorage,
                rootStorage,
                externalPatternInputSink
            );
            if (stepResult == PatternStepResult.COMPLETED) {
                LOGGER.debug("{} completed", pattern.getKey());
                completedPatterns.add(pattern.getValue());
                return stepResult;
            } else if (stepResult != PatternStepResult.IDLE) {
                result = PatternStepResult.RUNNING;
            }
        }
        return result;
    }

    private boolean returnInternalStorageAndTryCompleteTask(final RootStorage rootStorage) {
        boolean returnedAll = true;
        boolean returnedAny = false;
        final Set<ResourceKey> internalResources = new HashSet<>(internalStorage.getAll());
        for (final ResourceKey internalResource : internalResources) {
            final long internalAmount = internalStorage.get(internalResource);
            final long inserted = rootStorage.insert(internalResource, internalAmount, Action.EXECUTE, Actor.EMPTY);
            if (inserted > 0) {
                returnedAny = true;
            }
            LOGGER.debug("Returned {}x {} into storage", inserted, internalResource);
            if (inserted != internalAmount) {
                returnedAll = false;
            }
            if (inserted > 0) {
                internalStorage.remove(internalResource, inserted);
            }
        }
        if (returnedAll) {
            updateState(TaskState.COMPLETED);
        }
        return returnedAny;
    }

    @Override
    public Collection<ResourceAmount> copyInternalStorageState() {
        return internalStorage.copyState();
    }

    @Override
    public InterceptResult beforeInsert(final ResourceKey insertedResource,
                                        final long insertedAmount,
                                        final Actor actor) {
        long reserved = 0;
        long intercepted = 0;
        for (final AbstractTaskPattern pattern : patterns.values()) {
            final long remainder = insertedAmount - reserved;
            final InterceptResult result = pattern.interceptInsertion(insertedResource, remainder);
            if (result.intercepted() > 0) {
                internalStorage.add(insertedResource, result.intercepted());
            }
            reserved += result.reserved();
            intercepted += result.intercepted();
            if (reserved == insertedAmount) {
                return new InterceptResult(reserved, intercepted);
            }
        }
        return new InterceptResult(reserved, intercepted);
    }

    @Override
    public void changed(final MutableResourceList.OperationResult change) {
        // no op
    }
}
