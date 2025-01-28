package com.refinedmods.refinedstorage.api.network.node;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.11")
@FunctionalInterface
public interface SchedulingMode {
    void execute(List<? extends ScheduledTask> tasks);

    @FunctionalInterface
    interface ScheduledTask {
        boolean run();
    }
}
