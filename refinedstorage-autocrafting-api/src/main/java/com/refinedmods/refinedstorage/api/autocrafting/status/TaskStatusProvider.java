package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.10")
public interface TaskStatusProvider {
    List<TaskStatus> getStatuses();

    void addListener(TaskStatusListener listener);

    void removeListener(TaskStatusListener listener);

    void cancel(TaskId taskId);

    void cancelAll();
}
