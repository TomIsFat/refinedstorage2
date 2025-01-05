package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.nodefactory.PatternProviderNetworkNodeFactory;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class PatternProviderNetworkNodeTest {
    private static final Pattern PATTERN_A = pattern().output(A, 1).ingredient(C, 1).build();
    private static final Pattern PATTERN_B = pattern().output(B, 1).ingredient(C, 1).build();

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 3)
    })
    private PatternProviderNetworkNode sut;

    @Test
    void testDefaultState(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldSetPatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Act
        sut.setPattern(0, PATTERN_A);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(A);
    }

    @Test
    void shouldRemovePatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);

        // Act
        sut.setPattern(0, null);

        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldReplacePatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);

        // Act
        sut.setPattern(0, PATTERN_B);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(B);
    }

    @Test
    void shouldRemovePatternsFromNetworkWhenInactive(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);

        // Act
        sut.setActive(false);

        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldAddPatternsFromNetworkWhenActive(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);
        sut.setActive(false);

        // Act
        sut.setActive(true);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(A);
    }

    @Test
    void shouldNotStepTasksWithoutNetwork(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) throws ExecutionException, InterruptedException {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false).get()).isTrue();

        // Act
        sut.setNetwork(null);
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);
    }

    @Test
    void shouldNotStepTasksWhenInactive(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) throws ExecutionException, InterruptedException {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false).get()).isTrue();

        // Act
        sut.setActive(false);
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);
    }

    @Test
    void shouldStepTasks(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) throws ExecutionException, InterruptedException {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false).get()).isTrue();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 1)
        );
        assertThat(sut.getTasks()).isEmpty();
    }

    @Test
    void shouldUseProviderAsSinkForExternalPatternInputsWhenSinkIsAttached(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) throws ExecutionException, InterruptedException {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage sinkContents = new StorageImpl();

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 1).build());
        sut.setExternalPatternInputSink((resources, action) -> {
            if (action == Action.EXECUTE) {
                resources.forEach(resource ->
                    sinkContents.insert(resource.resource(), resource.amount(), Action.EXECUTE, Actor.EMPTY));
            }
            return true;
        });
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false).get()).isTrue();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sinkContents.getAll()).isEmpty();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).isEmpty();

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sinkContents.getAll()).isEmpty();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sinkContents.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).isEmpty();
    }

    @Test
    void shouldUseProviderAsSinkForExternalPatternInputsWhenSinkIsAttachedAndDoesNotAcceptResources(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) throws ExecutionException, InterruptedException {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 1).build());
        sut.setExternalPatternInputSink((resources, action) -> false);
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false).get()).isTrue();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).isEmpty();

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );
    }

    @Test
    void shouldNotUseProviderAsSinkForExternalPatternInputsWhenThereIsNoSinkAttached(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) throws ExecutionException, InterruptedException {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false).get()).isTrue();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).isEmpty();

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );
    }

    @Test
    void shouldInterceptNetworkInsertionsWhenWaitingForExternalPatternOutputs(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) throws ExecutionException, InterruptedException {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 5).build());
        // swallow resources
        sut.setExternalPatternInputSink((resources, action) -> true);

        // Act & assert
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false).get()).isTrue();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );

        storage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(B, 3));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );

        storage.insert(B, 4, Action.EXECUTE, Actor.EMPTY);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(B, 5));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 2)
        );

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(sut.getTasks().getFirst().copyInternalStorageState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(B, 5));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 2)
        );

        sut.doWork();
        assertThat(sut.getTasks()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 7)
        );
    }

    @Nested
    @SetupNetwork(id = "other")
    class NetworkChangeTest {
        @Test
        void shouldInterceptInsertionsOnNewNetworkWhenNetworkChanges(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting,
            @InjectNetwork("other") final Network otherNetwork,
            @InjectNetworkStorageComponent(networkId = "other") final StorageNetworkComponent otherStorage
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

            otherStorage.addSource(new StorageImpl());

            sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 5).build());
            // swallow resources
            sut.setExternalPatternInputSink((resources, action) -> true);

            // Act & assert
            assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false).join()).isTrue();

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut.getTasks().getFirst().copyInternalStorageState()).containsExactlyInAnyOrder(
                new ResourceAmount(A, 3)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A, 7)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut.getTasks().getFirst().copyInternalStorageState()).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A, 7)
            );

            sut.setNetwork(otherNetwork);
            storage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut.getTasks().getFirst().copyInternalStorageState()).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A, 7),
                new ResourceAmount(B, 3)
            );
            assertThat(otherStorage.getAll()).isEmpty();

            otherStorage.insert(B, 4, Action.EXECUTE, Actor.EMPTY);
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut.getTasks().getFirst().copyInternalStorageState())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ResourceAmount(B, 4));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A, 7),
                new ResourceAmount(B, 3)
            );
            assertThat(otherStorage.getAll()).isEmpty();

            otherStorage.insert(B, 2, Action.EXECUTE, Actor.EMPTY);
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut.getTasks().getFirst().copyInternalStorageState())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ResourceAmount(B, 5));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A, 7),
                new ResourceAmount(B, 3)
            );
            assertThat(otherStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(B, 1)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);

            sut.doWork();
            assertThat(sut.getTasks()).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A, 7),
                new ResourceAmount(B, 3)
            );
            assertThat(otherStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(B, 6)
            );
        }
    }

    @Nested
    class PriorityTest {
        @AddNetworkNode(properties = {
            @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 3)
        })
        private PatternProviderNetworkNode other;

        @Test
        void shouldNotUseProviderAsSinkForExternalChildPatternWhenProviderIsRemovedAndRootProviderIsStillPresent(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

            final Pattern patternForA = pattern().output(A, 1).ingredient(B, 1).build();
            sut.setPattern(0, patternForA);

            final Pattern patternForB = pattern(PatternType.EXTERNAL)
                .output(B, 1)
                .ingredient(C, 1)
                .build();
            other.setPattern(0, patternForB);

            // Act & assert
            assertThat(autocrafting.startTask(A, 1, Actor.EMPTY, false).join()).isTrue();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut.getTasks().getFirst().copyInternalStorageState()).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 10)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut.getTasks().getFirst().copyInternalStorageState())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ResourceAmount(C, 1));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 9)
            );

            autocrafting.onContainerRemoved(() -> other);

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut.getTasks().getFirst().copyInternalStorageState())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ResourceAmount(C, 1));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 9)
            );
        }

        @Test
        void shouldSetPatternsRespectingPriority(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            // Act
            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithHighestPriority, patternWithLowestPriority);
        }

        @Test
        void shouldRemovePatternsRespectingPriorityWhenInactive(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Act
            other.setActive(false);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactly(A);
            assertThat(autocrafting.getPatternsByOutput(A)).containsExactly(patternWithLowestPriority);
        }

        @Test
        void shouldAddPatternsRespectingPriorityWhenActive(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            sut.setActive(false);
            other.setActive(false);

            // Act
            sut.setActive(true);
            other.setActive(true);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithHighestPriority, patternWithLowestPriority);
        }

        @Test
        void shouldModifyPriorityAfterAddingPatterns(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Act
            sut.setPriority(2);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithLowestPriority, patternWithHighestPriority);
        }
    }
}
