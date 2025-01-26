package com.refinedmods.refinedstorage.api.autocrafting.task;

enum PatternStepResult {
    COMPLETED,
    RUNNING,
    IDLE;

    PatternStepResult and(final PatternStepResult stepResult) {
        if (this == IDLE) {
            return stepResult;
        } else if (this == COMPLETED) {
            return COMPLETED;
        } else {
            return stepResult == COMPLETED ? COMPLETED : RUNNING;
        }
    }

    boolean isChanged() {
        return this != IDLE;
    }
}
