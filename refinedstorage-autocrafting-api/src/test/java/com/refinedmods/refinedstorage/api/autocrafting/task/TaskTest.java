package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingHelpers.patterns;
import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingHelpers.storage;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SIGN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.task.Patterns.CRAFTING_TABLE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.Patterns.OAK_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.Patterns.SPRUCE_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskCraftingCalculatorListener.calculatePlan;
import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {
    private static Task getTask(final RootStorage storage,
                                final PatternRepository patterns,
                                final ResourceKey resource,
                                final long amount) {
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);
        return calculatePlan(sut, resource, amount).map(Task::new).orElseThrow();
    }

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
        task.step(storage);

        // Assert
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
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
        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_PLANKS, 2, Action.EXECUTE, Actor.EMPTY);
        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_PLANKS, 3, Action.EXECUTE, Actor.EMPTY);
        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(OAK_PLANKS, 1)
        );
    }

    @Test
    void shouldCompleteTask() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getTask(storage, patterns, CRAFTING_TABLE, 3);
        task.step(storage);

        // Act & assert
        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 4),
                new ResourceAmount(OAK_PLANKS, 8)
            );

        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(CRAFTING_TABLE, 1),
                new ResourceAmount(SPRUCE_PLANKS, 3),
                new ResourceAmount(OAK_PLANKS, 5)
            );

        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(CRAFTING_TABLE, 2),
                new ResourceAmount(SPRUCE_PLANKS, 2),
                new ResourceAmount(OAK_PLANKS, 2)
            );

        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(CRAFTING_TABLE, 3)
            );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 3)
        );

        task.step(storage);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
    }

    @Test
    void shouldPartiallyReturnInternalStorage() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(SIGN, 10)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final Task task = getTask(storage, patterns, CRAFTING_TABLE, 3);
        task.step(storage);

        // Act & assert
        for (int i = 0; i < 4; ++i) {
            assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
            task.step(storage);
        }
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        final RootStorage returnStorage = new RootStorageImpl();
        returnStorage.addSource(new LimitedStorageImpl(2));

        task.step(returnStorage);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(CRAFTING_TABLE, 2)
        );

        task.step(returnStorage);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(CRAFTING_TABLE, 2)
        );

        returnStorage.addSource(new StorageImpl());
        task.step(returnStorage);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
    }
}
