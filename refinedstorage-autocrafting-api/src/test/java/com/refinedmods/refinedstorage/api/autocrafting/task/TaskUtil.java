package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlanCraftingCalculatorListener.calculatePlan;
import static org.assertj.core.api.Assertions.assertThat;

final class TaskUtil {
    private TaskUtil() {
    }

    static Task getTask(final RootStorage storage,
                        final PatternRepository patterns,
                        final ResourceKey resource,
                        final long amount) {
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);
        final Task task = calculatePlan(sut, resource, amount).map(TaskImpl::fromPlan).orElseThrow();
        storage.addListener(task);
        return task;
    }

    static Task getRunningTask(final RootStorage storage,
                               final PatternRepository patterns,
                               final ExternalPatternInputSink externalPatternInputSink,
                               final ResourceKey resource,
                               final long amount) {
        final Task task = getTask(storage, patterns, resource, amount);
        assertThat(task.getState()).isEqualTo(TaskState.READY);
        task.step(storage, externalPatternInputSink);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        return task;
    }
}
