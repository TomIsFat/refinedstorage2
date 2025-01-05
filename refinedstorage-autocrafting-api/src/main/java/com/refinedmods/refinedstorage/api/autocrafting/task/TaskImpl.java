package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskImpl implements Task, RootStorageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskImpl.class);

    private final TaskId id = TaskId.create();
    private final Map<Pattern, AbstractTaskPattern> patterns;
    private final MutableResourceList initialRequirements = MutableResourceListImpl.create();
    private final MutableResourceList internalStorage = MutableResourceListImpl.create();
    private TaskState state = TaskState.READY;

    public TaskImpl(final TaskPlan plan) {
        this.patterns = plan.patterns().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> createTaskPattern(e.getKey(), e.getValue()),
            (a, b) -> a,
            LinkedHashMap::new
        ));
        plan.initialRequirements().forEach(initialRequirements::add);
    }

    private static AbstractTaskPattern createTaskPattern(final Pattern pattern,
                                                         final TaskPlan.PatternPlan patternPlan) {
        return switch (pattern.type()) {
            case INTERNAL -> new InternalTaskPattern(pattern, patternPlan);
            case EXTERNAL -> new ExternalTaskPattern(pattern, patternPlan);
        };
    }

    @Override
    public TaskState getState() {
        return state;
    }

    private void updateState(final TaskState newState) {
        LOGGER.info("Task {} state changed from {} to {}", id.id(), state, newState);
        this.state = newState;
    }

    @Override
    public void step(final RootStorage rootStorage, final ExternalPatternInputSink externalPatternInputSink) {
        switch (state) {
            case READY -> startTask(rootStorage);
            case EXTRACTING_INITIAL_RESOURCES -> extractInitialResourcesAndTryStartRunningTask(rootStorage);
            case RUNNING -> stepPatterns(externalPatternInputSink);
            case RETURNING_INTERNAL_STORAGE -> returnInternalStorageAndTryCompleteTask(rootStorage);
        }
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

    private void stepPatterns(final ExternalPatternInputSink externalPatternInputSink) {
        patterns.entrySet().removeIf(pattern -> {
            final boolean completed = pattern.getValue().step(internalStorage, externalPatternInputSink);
            if (completed) {
                LOGGER.info("{} completed", pattern.getKey());
            }
            return completed;
        });
        if (patterns.isEmpty()) {
            updateState(TaskState.RETURNING_INTERNAL_STORAGE);
        }
    }

    private void returnInternalStorageAndTryCompleteTask(final RootStorage rootStorage) {
        if (returnInternalStorage(rootStorage)) {
            updateState(TaskState.COMPLETED);
        }
    }

    Collection<ResourceAmount> copyInternalStorageState() {
        return internalStorage.copyState();
    }

    private boolean extractInitialResources(final RootStorage rootStorage) {
        boolean extractedAll = true;
        final Set<ResourceKey> resources = new HashSet<>(initialRequirements.getAll());
        for (final ResourceKey resource : resources) {
            final long needed = initialRequirements.get(resource);
            final long extracted = rootStorage.extract(resource, needed, Action.EXECUTE, Actor.EMPTY);
            LOGGER.info("Extracted {}x {} from storage", extracted, resource);
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
            LOGGER.info("Returned {}x {} into storage", inserted, resource);
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
    public long beforeInsert(final ResourceKey resource, final long amount, final Actor actor) {
        // TODO: coverage
        long totalIntercepted = 0;
        for (final AbstractTaskPattern pattern : patterns.values()) {
            final long remaining = amount - totalIntercepted;
            final long intercepted = pattern.interceptInsertion(resource, remaining);
            if (intercepted > 0) {
                internalStorage.add(resource, intercepted);
            }
            totalIntercepted += intercepted;
            if (totalIntercepted == amount) {
                break;
            }
        }
        return totalIntercepted;
    }

    @Override
    public void changed(final MutableResourceList.OperationResult change) {
        // no op
    }
}
