package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.10")
public interface TaskStatusListener {
    void taskStatusChanged(TaskStatus status);

    void taskRemoved(TaskId id);

    void taskAdded(TaskStatus status);
}
