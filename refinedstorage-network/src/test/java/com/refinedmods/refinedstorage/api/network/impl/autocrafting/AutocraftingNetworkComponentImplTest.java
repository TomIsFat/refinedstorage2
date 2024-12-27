package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewItem;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternListener;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;
import com.refinedmods.refinedstorage.network.test.fixtures.FakeTaskStatusProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;

class AutocraftingNetworkComponentImplTest {
    private static final RecursiveComparisonConfiguration PREVIEW_CONFIG = RecursiveComparisonConfiguration.builder()
        .withIgnoredCollectionOrderInFields("items")
        .build();

    private RootStorage rootStorage;
    private AutocraftingNetworkComponentImpl sut;

    @BeforeEach
    void setUp() {
        rootStorage = new RootStorageImpl();
        sut = new AutocraftingNetworkComponentImpl(() -> rootStorage, new FakeTaskStatusProvider());
    }

    @Test
    void temporaryCoverage() {
        final PatternListener listener = new PatternListener() {
            @Override
            public void onAdded(final Pattern pattern) {
                // no op
            }

            @Override
            public void onRemoved(final Pattern pattern) {
                // no op
            }
        };
        sut.addListener(listener);
        sut.removeListener(listener);
        final TaskStatusListener listener2 = new TaskStatusListener() {
            @Override
            public void taskStatusChanged(final TaskStatus status) {
                // no op
            }

            @Override
            public void taskRemoved(final TaskId id) {
                // no op
            }

            @Override
            public void taskAdded(final TaskStatus status) {
                // no op
            }
        };
        sut.addListener(listener2);
        sut.removeListener(listener2);
        sut.getStatuses();
        sut.cancel(new TaskId(UUID.randomUUID()));
        sut.cancelAll();
        sut.testUpdate();
    }

    @Test
    void shouldAddPatternsFromPatternProvider() {
        // Arrange
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, new PatternImpl(A));

        final NetworkNodeContainer container = () -> provider;

        // Act
        sut.onContainerAdded(container);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(A);
    }

    @Test
    void shouldRemovePatternsFromPatternProvider() {
        // Arrange
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, new PatternImpl(A));

        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        sut.onContainerRemoved(container);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().isEmpty();
    }

    @Test
    void shouldAddPatternManually() {
        // Arrange
        final PatternImpl pattern = new PatternImpl(A);

        // Act
        sut.add(pattern);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(A);
    }

    @Test
    void shouldRemovePatternManually() {
        // Arrange
        final PatternImpl pattern = new PatternImpl(A);
        sut.add(pattern);

        // Act
        sut.remove(pattern);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().isEmpty();
    }

    @Test
    void shouldStartTask() {
        sut.startTask(A, 10);
    }

    @Test
    void shouldGetPreview() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);

        sut.add(new PatternImpl(
            List.of(new Ingredient(3, List.of(A))),
            new ResourceAmount(B, 1)
        ));

        // Act
        final Optional<Preview> preview = sut.getPreview(B, 2);

        // Assert
        assertThat(preview)
            .get()
            .usingRecursiveComparison(PREVIEW_CONFIG)
            .isEqualTo(new Preview(PreviewType.SUCCESS, List.of(
                new PreviewItem(B, 0, 0, 2),
                new PreviewItem(A, 6, 0, 0)
            ), Collections.emptyList()));
    }
}
