package com.refinedmods.refinedstorage.network.test.fixtures;

import com.refinedmods.refinedstorage.api.autocrafting.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;

import java.util.List;

public class FakeTaskStatusProvider implements TaskStatusProvider {
    @Override
    public List<TaskStatus> getStatuses() {
        return List.of();
    }

    @Override
    public void addListener(final TaskStatusListener listener) {
        // no op
    }

    @Override
    public void removeListener(final TaskStatusListener listener) {
        // no op
    }

    @Override
    public void cancel(final TaskId taskId) {
        // no op
    }

    @Override
    public void cancelAll() {
        // no op
    }
}
