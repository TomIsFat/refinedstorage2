package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.patterns;
import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingUtil.storage;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.CRAFTING_TABLE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.CRAFTING_TABLE_YIELD_2X_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.IRON_INGOT_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.IRON_PICKAXE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.OAK_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.SMOOTH_STONE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.SPRUCE_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.STICKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.PatternFixtures.STONE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.COBBLESTONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_INGOT;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_ORE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_PICKAXE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SIGN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SMOOTH_STONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STICKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STONE_BRICKS;
import static com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternInputSinkBuilder.externalPatternInputSink;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlanCraftingCalculatorListener.calculatePlan;
import static org.assertj.core.api.Assertions.assertThat;

class TaskImplTest {
    private static final RecursiveComparisonConfiguration STATUS_CONFIG = RecursiveComparisonConfiguration.builder()
        .withIgnoredFields("info.startTime")
        .build();
    private static final ExternalPatternInputSink EMPTY_SINK = (pattern, resources, action) ->
        ExternalPatternInputSink.Result.SKIPPED;

    @Test
    void testInitialState() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);

        // Act
        final Task task = getTask(storage, patterns, CRAFTING_TABLE, 3);

        // Assert
        assertThat(task.getState()).isEqualTo(TaskState.READY);
        assertThat(task.getId()).isNotNull();
        assertThat(task.getId().toString()).isEqualTo(task.getId().id().toString());
    }

    @Test
    void shouldExtractAllResources() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getTask(storage, patterns, CRAFTING_TABLE, 3);

        // Act
        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);

        // Assert
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_LOG, 1),
                new ResourceAmount(SPRUCE_LOG, 1),
                new ResourceAmount(OAK_PLANKS, 4)
            );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );
    }

    @Test
    void shouldPartiallyExtractAllResources() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getTask(storage, patterns, CRAFTING_TABLE, 3);
        storage.extract(OAK_PLANKS, 4, Action.EXECUTE, Actor.EMPTY);

        // Act & assert
        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_PLANKS, 2, Action.EXECUTE, Actor.EMPTY);
        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_PLANKS, 3, Action.EXECUTE, Actor.EMPTY);
        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(OAK_PLANKS, 1)
        );
    }

    @Test
    void shouldCompleteTaskWithInternalPatterns() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK, CRAFTING_TABLE, 3);

        // Act & assert
        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(SIGN, 10)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 4),
                new ResourceAmount(OAK_PLANKS, 8)
            );

        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 1)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 3),
                new ResourceAmount(OAK_PLANKS, 5)
            );

        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 2)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 2),
                new ResourceAmount(OAK_PLANKS, 2)
            );

        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(task.copyInternalStorageState()).isEmpty();

        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
    }

    @Test
    void shouldPartiallyReturnOutputsWithInternalPatterns() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(
            OAK_PLANKS_PATTERN,
            SPRUCE_PLANKS_PATTERN,
            CRAFTING_TABLE_YIELD_2X_PATTERN
        );
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK, CRAFTING_TABLE, 4);

        final RootStorage returnStorage = new RootStorageImpl();
        returnStorage.addSource(new LimitedStorageImpl(3));

        // Act & assert
        task.step(returnStorage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(returnStorage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 4),
                new ResourceAmount(OAK_PLANKS, 4)
            );

        task.step(returnStorage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 2)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(SPRUCE_PLANKS, 2)
            );

        task.step(returnStorage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(CRAFTING_TABLE, 1)
            );

        task.step(returnStorage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(CRAFTING_TABLE, 1)
            );

        final RootStorage finalReturnStorage = new RootStorageImpl();
        finalReturnStorage.addSource(new StorageImpl());

        task.step(finalReturnStorage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(finalReturnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 1)
        );
        assertThat(task.copyInternalStorageState()).isEmpty();
    }

    @Test
    void shouldCompleteTaskWithExternalPatternAsChildPatternOfInternalPattern() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(STICKS, 2),
            new ResourceAmount(IRON_ORE, 3)
        );
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN, IRON_PICKAXE_PATTERN);
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final ExternalPatternInputSinkBuilder.Sink ironOreSink = sinkBuilder.storageSink(IRON_INGOT_PATTERN);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final Task task = getRunningTask(storage, patterns, sink, IRON_PICKAXE, 1);

        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 3),
                new ResourceAmount(STICKS, 2)
            );

        // Act & assert
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 2),
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 1)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 1),
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 2)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );

        storage.insert(IRON_INGOT, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_INGOT, 1),
                new ResourceAmount(STICKS, 2)
            );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_INGOT, 1),
                new ResourceAmount(STICKS, 2)
            );

        storage.insert(IRON_INGOT, 5, Action.EXECUTE, Actor.EMPTY);
        storage.insert(STONE, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(IRON_INGOT, 3),
            new ResourceAmount(STONE, 2)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_INGOT, 3),
                new ResourceAmount(STICKS, 2)
            );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(IRON_PICKAXE, 1),
            new ResourceAmount(IRON_INGOT, 3),
            new ResourceAmount(STONE, 2)
        );
        assertThat(task.copyInternalStorageState()).isEmpty();
    }

    @Test
    void shouldCompleteTaskWithExternalPatternsThatAreDependentOnEachOther() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(COBBLESTONE, 64)
        );
        final PatternRepository patterns = patterns(STONE_PATTERN, SMOOTH_STONE_PATTERN);
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final ExternalPatternInputSinkBuilder.Sink cobblestoneSink = sinkBuilder.storageSink(STONE_PATTERN);
        final ExternalPatternInputSinkBuilder.Sink stoneSink = sinkBuilder.storageSink(SMOOTH_STONE_PATTERN);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final Task task = getRunningTask(storage, patterns, sink, SMOOTH_STONE, 4);

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 60)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(COBBLESTONE, 4)
            );

        // Act & assert
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(COBBLESTONE, 3)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 1)
        );
        assertThat(stoneSink.getAll()).isEmpty();

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(COBBLESTONE, 2)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 2)
        );
        assertThat(stoneSink.getAll()).isEmpty();

        storage.insert(STONE, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(COBBLESTONE, 2),
                new ResourceAmount(STONE, 2)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 2)
        );
        assertThat(stoneSink.getAll()).isEmpty();

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(COBBLESTONE, 1),
                new ResourceAmount(STONE, 1)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 3)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 1)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 2)
        );

        storage.insert(STONE, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(STONE, 2)
            );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 2)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 1)
        );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 3)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 4)
        );

        storage.insert(SMOOTH_STONE, 4, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 4)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(COBBLESTONE, 60),
            new ResourceAmount(SMOOTH_STONE, 4)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 4)
        );
        assertThat(stoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 4)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(COBBLESTONE, 60),
            new ResourceAmount(SMOOTH_STONE, 4)
        );
    }

    @Test
    void shouldCompleteTaskWithExternalPatternsThatShareTheSameOutputResources() {
        // Arrange
        final Pattern aToStonePattern = pattern(PatternType.EXTERNAL)
            .ingredient(A, 1)
            .output(STONE, 1)
            .build();
        final Pattern stoneBricksPattern = pattern()
            .ingredient(STONE, 1)
            .ingredient(STONE, 1)
            .output(STONE_BRICKS, 1)
            .build();
        final RootStorage storage = storage(
            new ResourceAmount(COBBLESTONE, 1),
            new ResourceAmount(A, 1)
        );
        final PatternRepository patterns = patterns(STONE_PATTERN, aToStonePattern, stoneBricksPattern);
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final ExternalPatternInputSinkBuilder.Sink cobblestoneSink = sinkBuilder.storageSink(STONE_PATTERN);
        final ExternalPatternInputSinkBuilder.Sink aSink = sinkBuilder.storageSink(aToStonePattern);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final Task task = getRunningTask(storage, patterns, sink, STONE_BRICKS, 1);

        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(COBBLESTONE, 1),
                new ResourceAmount(A, 1)
            );

        // Act & assert
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 1)
        );
        assertThat(aSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );

        storage.insert(STONE, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(STONE, 2)
        );
        assertThat(cobblestoneSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(COBBLESTONE, 1)
        );
        assertThat(aSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
    }

    @Test
    void shouldNotCompleteTaskWithExternalPatternIfSinkDoesNotAcceptResources() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(STICKS, 2 * 2),
            new ResourceAmount(IRON_ORE, 3 * 2)
        );
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN, IRON_PICKAXE_PATTERN);
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final ExternalPatternInputSinkBuilder.Sink ironOreSink = sinkBuilder.storageSink(IRON_INGOT_PATTERN);
        ironOreSink.setEnabled(false);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final Task task = getRunningTask(storage, patterns, sink, IRON_PICKAXE, 2);

        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 3 * 2),
                new ResourceAmount(STICKS, 2 * 2)
            );

        // Act & assert
        for (int i = 0; i < 6; ++i) {
            task.step(storage, sink, StepBehavior.DEFAULT);
            assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
            assertThat(task.copyInternalStorageState())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                    new ResourceAmount(IRON_ORE, 3 * 2),
                    new ResourceAmount(STICKS, 2 * 2)
                );
        }

        ironOreSink.setEnabled(true);
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, (3 * 2) - 1),
                new ResourceAmount(STICKS, 2 * 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(IRON_ORE, 1)
        );
    }

    @Test
    void shouldNotCompleteTaskWithExternalPatternIfSinkDoesNotAcceptResourcesOnlyWhenExecuting() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(STICKS, 2),
            new ResourceAmount(IRON_ORE, 3)
        );
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN, IRON_PICKAXE_PATTERN);
        final ExternalPatternInputSink sink = (pattern, resources, action) ->
            action == Action.SIMULATE
                ? ExternalPatternInputSink.Result.ACCEPTED
                : ExternalPatternInputSink.Result.REJECTED;
        final Task task = getRunningTask(storage, patterns, sink, IRON_PICKAXE, 1);

        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 3),
                new ResourceAmount(STICKS, 2)
            );

        // Act & assert
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 2), // we have voided 1 iron ore
                new ResourceAmount(STICKS, 2)
            );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 1), // we have voided 1 iron ore
                new ResourceAmount(STICKS, 2)
            );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(STICKS, 2));

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(STICKS, 2));
    }

    @Test
    void shouldStepAccordingToCustomStepBehavior() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK, CRAFTING_TABLE, 10);

        // Act & assert
        task.step(storage, EMPTY_SINK, new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return pattern == OAK_PLANKS_PATTERN ? 3 : 1;
            }
        });
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_LOG, 10 - 3),
                new ResourceAmount(OAK_PLANKS, 4 * 3)
            );

        task.step(storage, EMPTY_SINK, new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return pattern == OAK_PLANKS_PATTERN ? 4 : (pattern == CRAFTING_TABLE_PATTERN ? 10 : 1);
            }
        });
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_LOG, 10 - 3 - 4),
                new ResourceAmount(OAK_PLANKS, 4 * 4)
            );

        task.step(storage, EMPTY_SINK, new StepBehavior() {
            @Override
            public boolean canStep(final Pattern pattern) {
                return pattern != CRAFTING_TABLE_PATTERN;
            }

            @Override
            public int getSteps(final Pattern pattern) {
                return pattern == OAK_PLANKS_PATTERN ? 999 : 1;
            }
        });
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 4 * 7)
            );

        task.step(storage, EMPTY_SINK, new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return 999;
            }
        });
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(CRAFTING_TABLE, 10)
        );
        assertThat(task.copyInternalStorageState()).isEmpty();
    }

    @Test
    void shouldReportStatusCorrectly() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 10),
            new ResourceAmount(IRON_ORE, 10)
        );
        final PatternRepository patterns = patterns(
            OAK_PLANKS_PATTERN,
            STICKS_PATTERN,
            IRON_INGOT_PATTERN,
            IRON_PICKAXE_PATTERN
        );
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final ExternalPatternInputSinkBuilder.Sink ironOreSink = sinkBuilder.storageSink(IRON_INGOT_PATTERN);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK, IRON_PICKAXE, 1);

        // Act & assert
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .scheduled(IRON_INGOT, 2)
                .processing(IRON_ORE, 1)
                .crafting(STICKS, 4)
                .stored(OAK_PLANKS, 4)
                .stored(IRON_ORE, 2)
                .build(0.2));
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 2),
                new ResourceAmount(OAK_PLANKS, 4)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 1)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .scheduled(IRON_INGOT, 1)
                .processing(IRON_ORE, 2)
                .stored(OAK_PLANKS, 2)
                .stored(IRON_ORE, 1)
                .stored(STICKS, 4)
                .build(0.5));
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 1),
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 4)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 2)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .processing(IRON_ORE, 3)
                .stored(OAK_PLANKS, 2)
                .stored(STICKS, 4)
                .build(0.6666666666666666));
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 4)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );

        storage.insert(IRON_INGOT, 2, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .processing(IRON_ORE, 1)
                .stored(OAK_PLANKS, 2)
                .stored(STICKS, 4)
                .stored(IRON_INGOT, 2)
                .build(0.6666666666666666));
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 4),
                new ResourceAmount(IRON_INGOT, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );

        storage.insert(IRON_INGOT, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .crafting(IRON_PICKAXE, 1)
                .stored(OAK_PLANKS, 2)
                .stored(STICKS, 4)
                .stored(IRON_INGOT, 3)
                .build(0.6666666666666666));
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 4),
                new ResourceAmount(IRON_INGOT, 3)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 9),
            new ResourceAmount(IRON_ORE, 7)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0)
                .stored(OAK_PLANKS, 2)
                .stored(STICKS, 2)
                .build(1.0));
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(OAK_PLANKS, 2),
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 9),
            new ResourceAmount(IRON_ORE, 7),
            new ResourceAmount(IRON_PICKAXE, 1)
        );

        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_PICKAXE, 1, 0).build(1.0)
        );
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 9),
            new ResourceAmount(IRON_ORE, 7),
            new ResourceAmount(IRON_PICKAXE, 1),
            new ResourceAmount(OAK_PLANKS, 2),
            new ResourceAmount(STICKS, 2)
        );
    }

    @Test
    void shouldReportWhetherSinkIsRejectingInputsOnStatus() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(IRON_ORE, 10));
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN);
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final ExternalPatternInputSinkBuilder.Sink ironOreSink = sinkBuilder.storageSink(IRON_INGOT_PATTERN);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK, IRON_INGOT, 2);

        // Act & assert
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1)
                .build(0));

        ironOreSink.setEnabled(false);
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1)
                .rejected(IRON_INGOT)
                .build(0));
    }

    @Test
    void shouldReportWhetherSinkIsNotFoundOnStatus() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(IRON_ORE, 10));
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN);
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final ExternalPatternInputSinkBuilder.Sink ironOreSink = sinkBuilder.storageSink(IRON_INGOT_PATTERN);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK, IRON_INGOT, 2);

        // Act & assert
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1)
                .build(0));

        ironOreSink.setEnabled(false);
        task.step(storage, EMPTY_SINK, StepBehavior.DEFAULT);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1)
                .noneFound(IRON_INGOT)
                .build(0));
    }

    @Test
    void shouldReportWhetherSinkIsLockedFoundOnStatus() {
        // Arrange
        final RootStorage storage = storage(new ResourceAmount(IRON_ORE, 10));
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN);
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final ExternalPatternInputSinkBuilder.Sink ironOreSink = sinkBuilder.storageSink(IRON_INGOT_PATTERN);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final Task task = getRunningTask(storage, patterns, EMPTY_SINK, IRON_INGOT, 2);

        // Act & assert
        task.step(storage, sink, StepBehavior.DEFAULT);
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1)
                .build(0));

        ironOreSink.setEnabled(false);
        task.step(
            storage,
            (pattern, resources, action) -> ExternalPatternInputSink.Result.LOCKED,
            StepBehavior.DEFAULT
        );
        assertThat(task.getStatus()).usingRecursiveComparison(STATUS_CONFIG).isEqualTo(
            new TaskStatusBuilder(task.getId(), IRON_INGOT, 2, 0)
                .scheduled(IRON_INGOT, 1)
                .stored(IRON_ORE, 1)
                .processing(IRON_ORE, 1)
                .locked(IRON_INGOT)
                .build(0));
    }

    private static Task getTask(final RootStorage storage,
                                final PatternRepository patterns,
                                final ResourceKey resource,
                                final long amount) {
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);
        final Task task = calculatePlan(sut, resource, amount).map(plan -> new TaskImpl(
            plan,
            MutableResourceListImpl.orderPreserving()
        )).orElseThrow();
        storage.addListener(task);
        return task;
    }

    private static Task getRunningTask(final RootStorage storage,
                                       final PatternRepository patterns,
                                       final ExternalPatternInputSink externalPatternInputSink,
                                       final ResourceKey resource,
                                       final long amount) {
        final Task task = getTask(storage, patterns, resource, amount);
        assertThat(task.getState()).isEqualTo(TaskState.READY);
        task.step(storage, externalPatternInputSink, StepBehavior.DEFAULT);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        return task;
    }
}
