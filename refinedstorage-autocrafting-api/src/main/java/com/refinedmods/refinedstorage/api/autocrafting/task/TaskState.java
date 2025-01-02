package com.refinedmods.refinedstorage.api.autocrafting.task;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public enum TaskState {
    READY,
    EXTRACTING_INITIAL_RESOURCES,
    RUNNING,
    RETURNING_INTERNAL_STORAGE,
    COMPLETED
}
