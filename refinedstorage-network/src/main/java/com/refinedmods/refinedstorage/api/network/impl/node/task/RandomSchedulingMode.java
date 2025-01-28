package com.refinedmods.refinedstorage.api.network.impl.node.task;

import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;

import java.util.ArrayList;
import java.util.List;

public class RandomSchedulingMode implements SchedulingMode {
    private final Randomizer randomizer;

    public RandomSchedulingMode(final Randomizer randomizer) {
        this.randomizer = randomizer;
    }

    @Override
    public void execute(final List<? extends ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        final List<ScheduledTask> shuffledTasks = new ArrayList<>(tasks);
        randomizer.shuffle(shuffledTasks);
        for (final ScheduledTask task : shuffledTasks) {
            if (task.run()) {
                return;
            }
        }
    }

    public interface Randomizer {
        void shuffle(List<ScheduledTask> list);
    }
}
