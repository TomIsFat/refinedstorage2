package com.refinedmods.refinedstorage.api.autocrafting.task;

enum PatternStepResult {
    COMPLETED,
    RUNNING,
    IDLE;

    boolean isChanged() {
        return this != IDLE;
    }
}
