package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewItem;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.NetworkImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.network.test.fixtures.NetworkTestFixtures;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static org.assertj.core.api.Assertions.assertThat;

class AutocraftingNetworkComponentImplTest {
    private Network network;
    private RootStorage rootStorage;
    private AutocraftingNetworkComponentImpl sut;

    @BeforeEach
    void setUp() {
        network = new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY);
        rootStorage = network.getComponent(StorageNetworkComponent.class);
        sut = new AutocraftingNetworkComponentImpl(() -> rootStorage, Executors.newSingleThreadExecutor());
    }

    @Test
    void shouldAddPatternsFromPatternProvider() {
        // Arrange
        final PatternBuilder patternABuilder = pattern().output(A, 1).ingredient(C, 1);
        final PatternBuilder patternBBuilder = pattern().output(B, 1).ingredient(C, 1);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern1 = patternABuilder.build();
        provider1.setPattern(1, pattern1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern2 = patternABuilder.build();
        final Pattern pattern3 = patternBBuilder.build();
        provider2.setPattern(1, pattern2);
        provider2.setPattern(2, pattern3);

        // Act
        sut.onContainerAdded(() -> provider1);
        sut.onContainerAdded(() -> provider1);
        sut.onContainerAdded(() -> provider2);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(A, B);
        assertThat(sut.getProviderByPattern(pattern1)).isEqualTo(provider1);
        assertThat(sut.getProviderByPattern(pattern2)).isEqualTo(provider2);
        assertThat(sut.getProviderByPattern(pattern3)).isEqualTo(provider2);
        assertThat(sut.getProviderByPattern(patternABuilder.build())).isNull();
        assertThat(sut.getSinksByPatternLayout(pattern1.layout())).containsExactlyInAnyOrder(provider1, provider2);
        assertThat(sut.getSinksByPatternLayout(pattern2.layout())).containsExactlyInAnyOrder(provider1, provider2);
        assertThat(sut.getSinksByPatternLayout(pattern3.layout())).containsExactly(provider2);
        assertThat(sut.getSinksByPatternLayout(pattern().output(D, 1).ingredient(C, 1).buildLayout()))
            .isEmpty();
    }

    @Test
    void shouldRemovePatternsFromPatternProvider() {
        // Arrange
        final PatternBuilder patternABuilder = pattern().output(A, 1).ingredient(C, 1);
        final PatternBuilder patternBBuilder = pattern().output(B, 1).ingredient(C, 1);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern1 = patternABuilder.build();
        provider1.setPattern(1, pattern1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern2 = patternABuilder.build();
        final Pattern pattern3 = patternBBuilder.build();
        provider2.setPattern(1, pattern2);
        provider2.setPattern(2, pattern3);

        sut.onContainerAdded(() -> provider1);
        sut.onContainerAdded(() -> provider1);
        sut.onContainerAdded(() -> provider2);

        // Act & assert
        sut.onContainerRemoved(() -> provider1);
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(A, B);
        assertThat(sut.getProviderByPattern(pattern1)).isNull();
        assertThat(sut.getProviderByPattern(pattern2)).isEqualTo(provider2);
        assertThat(sut.getProviderByPattern(pattern3)).isEqualTo(provider2);
        assertThat(sut.getSinksByPatternLayout(pattern1.layout())).containsExactly(provider2);
        assertThat(sut.getSinksByPatternLayout(pattern2.layout())).containsExactly(provider2);
        assertThat(sut.getSinksByPatternLayout(pattern3.layout())).containsExactly(provider2);

        sut.onContainerRemoved(() -> provider2);
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getProviderByPattern(pattern1)).isNull();
        assertThat(sut.getProviderByPattern(pattern2)).isNull();
        assertThat(sut.getProviderByPattern(pattern3)).isNull();
        assertThat(sut.getSinksByPatternLayout(pattern1.layout())).isEmpty();
        assertThat(sut.getSinksByPatternLayout(pattern2.layout())).isEmpty();
        assertThat(sut.getSinksByPatternLayout(pattern3.layout())).isEmpty();

        sut.onContainerRemoved(() -> provider2);
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getProviderByPattern(pattern1)).isNull();
        assertThat(sut.getProviderByPattern(pattern2)).isNull();
        assertThat(sut.getProviderByPattern(pattern3)).isNull();
        assertThat(sut.getSinksByPatternLayout(pattern1.layout())).isEmpty();
        assertThat(sut.getSinksByPatternLayout(pattern2.layout())).isEmpty();
        assertThat(sut.getSinksByPatternLayout(pattern3.layout())).isEmpty();
    }

    @Test
    void shouldGetPreview() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<Preview> preview = sut.getPreview(B, 2).join();

        // Assert
        assertThat(preview).get().usingRecursiveComparison().isEqualTo(new Preview(PreviewType.SUCCESS, List.of(
            new PreviewItem(B, 0, 0, 2),
            new PreviewItem(A, 6, 0, 0)
        ), Collections.emptyList()));
    }

    @Test
    void shouldGetMaxAmount() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 64, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 4).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final long maxAmount = sut.getMaxAmount(B).join();

        // Assert
        assertThat(maxAmount).isEqualTo(16);
    }

    @Test
    void shouldStartTask() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TaskId> taskId = sut.startTask(B, 1, Actor.EMPTY, false).join();

        // Assert
        assertThat(taskId).isPresent();
        assertThat(provider.getTasks()).hasSize(1);
    }

    @Test
    void shouldCancelTask() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        provider1.setActive(true);
        provider1.setNetwork(network);
        provider1.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.setActive(true);
        provider2.setNetwork(network);
        provider2.setPattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final Optional<TaskId> taskId1 = sut.startTask(B, 1, Actor.EMPTY, false).join();
        final Optional<TaskId> taskId2 = sut.startTask(C, 1, Actor.EMPTY, false).join();

        assertThat(taskId1).isPresent();
        assertThat(taskId2).isPresent();
        assertThat(provider1.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);

        // Act & assert
        provider1.doWork();
        provider2.doWork();
        assertThat(provider1.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );

        sut.cancel(taskId1.get());
        assertThat(provider1.getTasks()).hasSize(1)
            .allMatch(t -> t.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );

        provider1.doWork();
        assertThat(provider1.getTasks()).isEmpty();
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );

        sut.cancel(taskId1.get());
    }

    @Test
    void shouldCancelAllTasks() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        provider1.setActive(true);
        provider1.setNetwork(network);
        provider1.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.setActive(true);
        provider2.setNetwork(network);
        provider2.setPattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final Optional<TaskId> taskId1 = sut.startTask(B, 1, Actor.EMPTY, false).join();
        final Optional<TaskId> taskId2 = sut.startTask(C, 1, Actor.EMPTY, false).join();

        assertThat(taskId1).isPresent();
        assertThat(taskId2).isPresent();
        assertThat(provider1.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);

        // Act & assert
        provider1.doWork();
        provider2.doWork();
        assertThat(provider1.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );

        sut.cancelAll();
        assertThat(provider1.getTasks()).hasSize(1)
            .allMatch(t -> t.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(provider2.getTasks()).hasSize(1)
            .allMatch(t -> t.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );

        provider1.doWork();
        provider2.doWork();
        assertThat(provider1.getTasks()).isEmpty();
        assertThat(provider2.getTasks()).isEmpty();
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );

        sut.cancelAll();
    }

    @Test
    void shouldNotStartTaskWhenThereAreMissingIngredients() {
        // Arrange
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TaskId> taskId = sut.startTask(B, 2, Actor.EMPTY, false).join();

        // Assert
        assertThat(taskId).isEmpty();
        assertThat(provider.getTasks()).isEmpty();
    }

    @Test
    void shouldGetAllTaskStatuses() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        provider1.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.setPattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final PatternProviderNetworkNode provider3 = new PatternProviderNetworkNode(0, 5);
        provider3.setPattern(1, pattern().ingredient(A, 3).output(D, 1).build());
        sut.onContainerAdded(() -> provider3);

        final Optional<TaskId> taskId1 = sut.startTask(B, 1, Actor.EMPTY, false).join();
        final Optional<TaskId> taskId2 = sut.startTask(C, 1, Actor.EMPTY, false).join();

        sut.startTask(D, 1, Actor.EMPTY, false).join();
        sut.onContainerRemoved(() -> provider3);

        // Act
        final List<TaskStatus> taskStatuses = sut.getStatuses();

        // Assert
        assertThat(taskId1).isPresent();
        assertThat(taskId2).isPresent();
        assertThat(provider1.getTasks()).hasSize(1);
        assertThat(provider2.getTasks()).hasSize(1);
        assertThat(taskStatuses)
            .hasSize(2)
            .anyMatch(ts -> ts.info().id().equals(taskId1.get()))
            .anyMatch(ts -> ts.info().id().equals(taskId2.get()));
    }
}
