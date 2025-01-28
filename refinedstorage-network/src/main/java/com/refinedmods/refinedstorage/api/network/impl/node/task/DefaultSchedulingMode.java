package com.refinedmods.refinedstorage.api.network.impl.node.task;

import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;

import java.util.List;

public class DefaultSchedulingMode implements SchedulingMode {
    @Override
    public void execute(final List<? extends ScheduledTask> tasks) {
        for (final ScheduledTask task : tasks) {
            if (task.run()) {
                return;
            }
        }
    }
}
