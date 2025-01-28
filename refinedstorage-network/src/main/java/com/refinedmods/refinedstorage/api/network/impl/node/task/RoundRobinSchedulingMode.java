package com.refinedmods.refinedstorage.api.network.impl.node.task;

import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;

import java.util.List;

public class RoundRobinSchedulingMode implements SchedulingMode {
    private final State state;

    public RoundRobinSchedulingMode(final State state) {
        this.state = state;
    }

    @Override
    public void execute(final List<? extends ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        final int startIndex = state.getIndex() % tasks.size();
        for (int i = startIndex; i < tasks.size(); ++i) {
            final ScheduledTask task = tasks.get(i);
            if (task.run()) {
                state.setIndex((state.getIndex() + 1) % tasks.size());
                return;
            }
        }
        state.setIndex(0);
    }

    public int getIndex() {
        return state.getIndex();
    }

    public static class State {
        private final Runnable callback;
        private int index;

        public State(final Runnable callback, final int index) {
            this.index = index;
            this.callback = callback;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(final int index) {
            final boolean didChange = this.index != index;
            this.index = index;
            if (didChange) {
                callback.run();
            }
        }
    }
}
