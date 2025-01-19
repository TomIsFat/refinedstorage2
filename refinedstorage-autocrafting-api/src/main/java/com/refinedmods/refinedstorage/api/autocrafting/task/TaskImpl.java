package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    private final MutableResourceList initialRequirements = MutableResourceListImpl.create();
    private final MutableResourceList internalStorage = MutableResourceListImpl.create();
    private TaskState state = TaskState.READY;

    private TaskImpl(final TaskPlan plan) {
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
        return new TaskImpl(plan);
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
    public void step(final RootStorage rootStorage,
                     final ExternalPatternInputSink externalPatternInputSink,
                     final StepBehavior stepBehavior) {
        switch (state) {
            case READY -> startTask(rootStorage);
            case EXTRACTING_INITIAL_RESOURCES -> extractInitialResourcesAndTryStartRunningTask(rootStorage);
            case RUNNING -> stepPatterns(rootStorage, externalPatternInputSink, stepBehavior);
            case RETURNING_INTERNAL_STORAGE -> returnInternalStorageAndTryCompleteTask(rootStorage);
        }
    }

    @Override
    public void cancel() {
        state = TaskState.RETURNING_INTERNAL_STORAGE;
    }

    private void startTask(final RootStorage rootStorage) {
        updateState(TaskState.EXTRACTING_INITIAL_RESOURCES);
        extractInitialResourcesAndTryStartRunningTask(rootStorage);
    }

    private void extractInitialResourcesAndTryStartRunningTask(final RootStorage rootStorage) {
        if (extractInitialResources(rootStorage)) {
            updateState(TaskState.RUNNING);
        }
    }

    private void stepPatterns(final RootStorage rootStorage,
                              final ExternalPatternInputSink externalPatternInputSink,
                              final StepBehavior stepBehavior) {
        patterns.entrySet().removeIf(
            pattern -> stepPattern(rootStorage, externalPatternInputSink, stepBehavior, pattern)
        );
        if (patterns.isEmpty()) {
            if (internalStorage.isEmpty()) {
                updateState(TaskState.COMPLETED);
            } else {
                updateState(TaskState.RETURNING_INTERNAL_STORAGE);
            }
        }
    }

    private boolean stepPattern(final RootStorage rootStorage,
                                final ExternalPatternInputSink externalPatternInputSink,
                                final StepBehavior stepBehavior,
                                final Map.Entry<Pattern, AbstractTaskPattern> pattern) {
        if (!stepBehavior.canStep(pattern.getKey())) {
            return false;
        }
        final int steps = stepBehavior.getSteps(pattern.getKey());
        for (int i = 0; i < steps; ++i) {
            final boolean completed = pattern.getValue().step(internalStorage, rootStorage, externalPatternInputSink);
            if (completed) {
                LOGGER.debug("{} completed", pattern.getKey());
                return true;
            }
        }
        return false;
    }

    private void returnInternalStorageAndTryCompleteTask(final RootStorage rootStorage) {
        if (returnInternalStorage(rootStorage)) {
            updateState(TaskState.COMPLETED);
        }
    }

    @Override
    public Collection<ResourceAmount> copyInternalStorageState() {
        return internalStorage.copyState();
    }

    private boolean extractInitialResources(final RootStorage rootStorage) {
        boolean extractedAll = true;
        final Set<ResourceKey> resources = new HashSet<>(initialRequirements.getAll());
        for (final ResourceKey resource : resources) {
            final long needed = initialRequirements.get(resource);
            final long extracted = rootStorage.extract(resource, needed, Action.EXECUTE, Actor.EMPTY);
            LOGGER.debug("Extracted {}x {} from storage", extracted, resource);
            if (extracted != needed) {
                extractedAll = false;
            }
            if (extracted > 0) {
                initialRequirements.remove(resource, extracted);
                internalStorage.add(resource, extracted);
            }
        }
        return extractedAll;
    }

    private boolean returnInternalStorage(final RootStorage rootStorage) {
        boolean returnedAll = true;
        final Set<ResourceKey> resources = new HashSet<>(internalStorage.getAll());
        for (final ResourceKey resource : resources) {
            final long amount = internalStorage.get(resource);
            final long inserted = rootStorage.insert(resource, amount, Action.EXECUTE, Actor.EMPTY);
            LOGGER.debug("Returned {}x {} into storage", inserted, resource);
            if (inserted != amount) {
                returnedAll = false;
            }
            if (inserted > 0) {
                internalStorage.remove(resource, inserted);
            }
        }
        return returnedAll;
    }

    @Override
    public InterceptResult beforeInsert(final ResourceKey resource, final long amount, final Actor actor) {
        long reserved = 0;
        long intercepted = 0;
        for (final AbstractTaskPattern pattern : patterns.values()) {
            final long remainder = amount - reserved;
            final InterceptResult result = pattern.interceptInsertion(resource, remainder);
            if (result.intercepted() > 0) {
                internalStorage.add(resource, result.intercepted());
            }
            reserved += result.reserved();
            intercepted += result.intercepted();
            if (reserved == amount) {
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
