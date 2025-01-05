package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_INGOT;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_ORE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_PICKAXE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STICKS;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskCraftingCalculatorListener.calculatePlan;
import static org.assertj.core.api.Assertions.assertThat;

final class TaskUtil {
    static final Pattern OAK_PLANKS_PATTERN = pattern()
        .ingredient(OAK_LOG, 1)
        .output(OAK_PLANKS, 4)
        .build();
    static final Pattern SPRUCE_PLANKS_PATTERN = pattern()
        .ingredient(SPRUCE_LOG, 1)
        .output(SPRUCE_PLANKS, 4)
        .build();
    static final Pattern CRAFTING_TABLE_PATTERN = pattern()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .output(CRAFTING_TABLE, 1)
        .build();
    static final Pattern IRON_INGOT_PATTERN = pattern(PatternType.EXTERNAL)
        .ingredient(IRON_ORE, 1)
        .output(IRON_INGOT, 1)
        .build();
    static final Pattern IRON_PICKAXE_PATTERN = pattern()
        .ingredient(IRON_INGOT, 1)
        .ingredient(IRON_INGOT, 1)
        .ingredient(IRON_INGOT, 1)
        .ingredient(STICKS, 2)
        .output(IRON_PICKAXE, 1)
        .build();

    private TaskUtil() {
    }

    static TaskImpl getTask(final RootStorage storage,
                            final PatternRepository patterns,
                            final ResourceKey resource,
                            final long amount) {
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);
        final TaskImpl task = calculatePlan(sut, resource, amount).map(TaskImpl::new).orElseThrow();
        storage.addListener(task);
        return task;
    }

    static TaskImpl getRunningTask(final RootStorage storage,
                                   final PatternRepository patterns,
                                   final ExternalPatternInputSink externalPatternInputSink,
                                   final ResourceKey resource,
                                   final long amount) {
        final TaskImpl task = getTask(storage, patterns, resource, amount);
        assertThat(task.getState()).isEqualTo(TaskState.READY);
        task.step(storage, externalPatternInputSink);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        return task;
    }

    static TaskImpl getTaskReadyToReturnInternalStorage(final RootStorage storage,
                                                        final PatternRepository patterns,
                                                        final ExternalPatternInputSink externalPatternInputSink,
                                                        final ResourceKey resource,
                                                        final long amount) {
        final TaskImpl task = getRunningTask(storage, patterns, externalPatternInputSink, resource, amount);
        int tries = 0;
        while (task.getState() != TaskState.RETURNING_INTERNAL_STORAGE && tries < 10) {
            task.step(storage, externalPatternInputSink);
            tries++;
        }
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE)
            .describedAs("Task did not reach RETURNING_INTERNAL_STORAGE state");
        return task;
    }
}
