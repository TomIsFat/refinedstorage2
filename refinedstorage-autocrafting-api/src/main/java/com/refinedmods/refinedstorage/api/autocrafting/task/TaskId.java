package com.refinedmods.refinedstorage.api.autocrafting.task;

import java.util.UUID;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.10")
public record TaskId(UUID id) {
    public static TaskId create() {
        return new TaskId(UUID.randomUUID());
    }
}
