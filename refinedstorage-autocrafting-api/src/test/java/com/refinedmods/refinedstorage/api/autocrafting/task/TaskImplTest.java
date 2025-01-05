package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingHelpers.patterns;
import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingHelpers.storage;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_INGOT;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_ORE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_PICKAXE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SIGN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STICKS;
import static com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternInputSinkBuilder.externalPatternInputSink;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.CRAFTING_TABLE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.IRON_INGOT_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.IRON_PICKAXE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.OAK_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.SPRUCE_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.getRunningTask;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.getTask;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.getTaskReadyToReturnInternalStorage;
import static org.assertj.core.api.Assertions.assertThat;

class TaskImplTest {
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
        final TaskImpl task = getTask(storage, patterns, CRAFTING_TABLE, 3);

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
        final TaskImpl task = getTask(storage, patterns, CRAFTING_TABLE, 3);

        // Act
        task.step(storage, ExternalPatternInputSink.EMPTY);

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
        final TaskImpl task = getTask(storage, patterns, CRAFTING_TABLE, 3);
        storage.extract(OAK_PLANKS, 4, Action.EXECUTE, Actor.EMPTY);

        // Act & assert
        task.step(storage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_PLANKS, 2, Action.EXECUTE, Actor.EMPTY);
        task.step(storage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.EXTRACTING_INITIAL_RESOURCES);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        storage.insert(OAK_PLANKS, 3, Action.EXECUTE, Actor.EMPTY);
        task.step(storage, ExternalPatternInputSink.EMPTY);
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
        final TaskImpl task = getRunningTask(storage, patterns, ExternalPatternInputSink.EMPTY, CRAFTING_TABLE, 3);

        // Act & assert
        task.step(storage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(SPRUCE_PLANKS, 4),
                new ResourceAmount(OAK_PLANKS, 8)
            );

        task.step(storage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(CRAFTING_TABLE, 1),
                new ResourceAmount(SPRUCE_PLANKS, 3),
                new ResourceAmount(OAK_PLANKS, 5)
            );

        task.step(storage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(CRAFTING_TABLE, 2),
                new ResourceAmount(SPRUCE_PLANKS, 2),
                new ResourceAmount(OAK_PLANKS, 2)
            );

        task.step(storage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(CRAFTING_TABLE, 3)
            );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        task.step(storage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 3)
        );

        task.step(storage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(task.copyInternalStorageState()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10),
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
    }

    @Test
    void shouldCompleteTaskWithExternalPattern() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(STICKS, 2),
            new ResourceAmount(IRON_ORE, 3)
        );
        final PatternRepository patterns = patterns(IRON_INGOT_PATTERN, IRON_PICKAXE_PATTERN);
        final ExternalPatternInputSinkBuilder sinkBuilder = externalPatternInputSink();
        final Storage ironOreSink = sinkBuilder.storageSink(IRON_INGOT_PATTERN);
        final ExternalPatternInputSink sink = sinkBuilder.build();
        final TaskImpl task = getRunningTask(storage, patterns, sink, IRON_PICKAXE, 1);

        assertThat(storage.getAll()).isEmpty();
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_ORE, 3),
                new ResourceAmount(STICKS, 2)
            );

        // Act & assert
        task.step(storage, sink);
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

        task.step(storage, sink);
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

        task.step(storage, sink);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(STICKS, 2)
            );
        assertThat(ironOreSink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_ORE, 3)
        );

        task.step(storage, sink);
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

        task.step(storage, sink);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_INGOT, 1),
                new ResourceAmount(STICKS, 2)
            );

        storage.insert(IRON_INGOT, 5, Action.EXECUTE, Actor.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(IRON_INGOT, 3)
        );
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(IRON_INGOT, 3),
                new ResourceAmount(STICKS, 2)
            );

        task.step(storage, sink);
        assertThat(task.getState()).isEqualTo(TaskState.RUNNING);
        assertThat(task.copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new ResourceAmount(IRON_PICKAXE, 1)
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
        final TaskImpl task = getTaskReadyToReturnInternalStorage(
            storage, patterns, ExternalPatternInputSink.EMPTY, CRAFTING_TABLE, 3
        );

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(SIGN, 10)
        );

        final RootStorage returnStorage = new RootStorageImpl();
        returnStorage.addSource(new LimitedStorageImpl(2));

        task.step(returnStorage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(CRAFTING_TABLE, 2)
        );

        task.step(returnStorage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(CRAFTING_TABLE, 2)
        );

        returnStorage.addSource(new StorageImpl());
        task.step(returnStorage, ExternalPatternInputSink.EMPTY);
        assertThat(task.getState()).isEqualTo(TaskState.COMPLETED);
        assertThat(returnStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(CRAFTING_TABLE, 3)
        );
    }
}
