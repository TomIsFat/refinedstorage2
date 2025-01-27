package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkProvider;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternProviderNetworkNode extends SimpleNetworkNode implements PatternProvider, TaskListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternProviderNetworkNode.class);

    private final Pattern[] patterns;
    private final Set<ParentContainer> parents = new HashSet<>();
    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private int priority;
    @Nullable
    private PatternProviderExternalPatternSink sink;
    @Nullable
    private ExternalPatternSinkKeyProvider sinkKeyProvider;
    private StepBehavior stepBehavior = StepBehavior.DEFAULT;
    @Nullable
    private PatternProviderListener listener;

    public PatternProviderNetworkNode(final long energyUsage, final int patterns) {
        super(energyUsage);
        this.patterns = new Pattern[patterns];
    }

    public void setSink(final PatternProviderExternalPatternSink sink) {
        this.sink = sink;
    }

    public void setPattern(final int index, @Nullable final Pattern pattern) {
        final Pattern oldPattern = patterns[index];
        if (oldPattern != null) {
            parents.forEach(parent -> parent.remove(this, oldPattern));
        }
        patterns[index] = pattern;
        if (pattern != null) {
            parents.forEach(parent -> parent.add(this, pattern, priority));
        }
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        if (this.network != null) {
            final StorageNetworkComponent storage = this.network.getComponent(StorageNetworkComponent.class);
            for (final Task task : tasks) {
                cleanupTask(task, storage);
            }
        }
        super.setNetwork(network);
        if (network != null) {
            final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
            for (final Task task : tasks) {
                setupTask(task, storage);
            }
        }
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (!newActive) {
            for (final Pattern pattern : patterns) {
                if (pattern != null) {
                    parents.forEach(parent -> parent.remove(this, pattern));
                }
            }
            return;
        }
        for (final Pattern pattern : patterns) {
            if (pattern != null) {
                parents.forEach(parent -> parent.add(this, pattern, priority));
            }
        }
    }

    @Override
    public void onAddedIntoContainer(final ParentContainer parentContainer) {
        parents.add(parentContainer);
        tasks.forEach(task -> parentContainer.taskAdded(this, task));
        for (final Pattern pattern : patterns) {
            if (pattern != null) {
                parentContainer.add(this, pattern, priority);
            }
        }
    }

    @Override
    public void onRemovedFromContainer(final ParentContainer parentContainer) {
        tasks.forEach(parentContainer::taskRemoved);
        parents.remove(parentContainer);
        for (final Pattern pattern : patterns) {
            if (pattern != null) {
                parentContainer.remove(this, pattern);
            }
        }
    }

    @Override
    public void addTask(final Task task) {
        tasks.add(task);
        if (network != null) {
            setupTask(task, network.getComponent(StorageNetworkComponent.class));
        }
        parents.forEach(parent -> parent.taskAdded(this, task));
    }

    @Override
    public void cancelTask(final TaskId taskId) {
        for (final Task task : tasks) {
            if (task.getId().equals(taskId)) {
                task.cancel();
                return;
            }
        }
        throw new IllegalArgumentException("Task %s not found".formatted(taskId));
    }

    @Override
    public List<TaskStatus> getTaskStatuses() {
        return tasks.stream().map(Task::getStatus).toList();
    }

    @Override
    public void receivedExternalIteration() {
        if (listener != null) {
            listener.receivedExternalIteration();
        }
    }

    @Override
    public void receivedExternalIteration(final Pattern pattern) {
        if (network == null) {
            return;
        }
        final AutocraftingNetworkComponent autocrafting = network.getComponent(AutocraftingNetworkComponent.class);
        final PatternProvider provider = autocrafting.getProviderByPattern(pattern);
        if (provider == null) {
            return;
        }
        provider.receivedExternalIteration();
    }

    private void setupTask(final Task task, final StorageNetworkComponent storage) {
        storage.addListener(task);
    }

    private void cleanupTask(final Task task, final StorageNetworkComponent storage) {
        storage.removeListener(task);
    }

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        if (sink == null) {
            return ExternalPatternSink.Result.SKIPPED;
        }
        return sink.accept(resources, action);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive()) {
            return;
        }
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        final ExternalPatternSinkProvider sinkProvider = network.getComponent(AutocraftingNetworkComponent.class);
        tasks.removeIf(task -> stepTask(task, storage, sinkProvider));
    }

    private boolean stepTask(final Task task,
                             final StorageNetworkComponent storage,
                             final ExternalPatternSinkProvider sinkProvider) {
        boolean changed;
        boolean completed;
        try {
            changed = task.step(storage, sinkProvider, stepBehavior, this);
            completed = task.getState() == TaskState.COMPLETED;
        } catch (final Exception e) {
            LOGGER.error("Exception while stepping task {} {}, removing task", task.getResource(), task.getAmount(), e);
            changed = false;
            completed = true;
        }
        if (completed) {
            cleanupTask(task, storage);
            parents.forEach(parent -> parent.taskRemoved(task));
        } else if (changed) {
            parents.forEach(parent -> parent.taskChanged(task));
        }
        return completed;
    }

    @Nullable
    @Override
    public ExternalPatternSinkKey getKey() {
        return sinkKeyProvider != null ? sinkKeyProvider.getKey() : null;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
        for (final Pattern pattern : patterns) {
            if (pattern != null) {
                parents.forEach(parent -> parent.update(pattern, priority));
            }
        }
    }

    public void setStepBehavior(final StepBehavior stepBehavior) {
        this.stepBehavior = stepBehavior;
    }

    public void setSinkKeyProvider(final ExternalPatternSinkKeyProvider provider) {
        this.sinkKeyProvider = provider;
    }

    public void setListener(final PatternProviderListener listener) {
        this.listener = listener;
    }
}
