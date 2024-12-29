package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskStatusProviderImpl implements TaskStatusProvider {
    private final List<TaskStatus> statuses = new ArrayList<>();
    private final Set<TaskStatusListener> listeners = new HashSet<>();

    @Override
    public List<TaskStatus> getStatuses() {
        return Collections.unmodifiableList(statuses);
    }

    @Override
    public void addListener(final TaskStatusListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(final TaskStatusListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void cancel(final TaskId taskId) {
        final TaskStatus status = statuses.stream()
            .filter(s -> s.info().id().equals(taskId))
            .findFirst()
            .orElse(null);
        if (status != null) {
            statuses.remove(status);
            listeners.forEach(l -> l.taskRemoved(taskId));
        }
    }

    @Override
    public void cancelAll() {
        final List<TaskStatus> copy = new ArrayList<>(statuses);
        statuses.clear();
        copy.forEach(s -> listeners.forEach(l -> l.taskRemoved(s.info().id())));
    }
}
