package com.refinedmods.refinedstorage.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.task.RoundRobinSchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RoundRobinExporterNetworkNodeTest extends AbstractExporterNetworkNodeTest {
    private Runnable listener;

    @Override
    @BeforeEach
    void setUp() {
        listener = mock(Runnable.class);
        super.setUp();
    }

    @Override
    protected SchedulingMode createSchedulingMode() {
        return new RoundRobinSchedulingMode(new RoundRobinSchedulingMode.State(listener, 0));
    }

    @Test
    void shouldTransfer(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 95),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );

        verify(listener, times(1)).run();

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 95),
            new ResourceAmount(B, 95)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5)
        );

        verify(listener, times(2)).run();

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 90),
            new ResourceAmount(B, 95)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 5)
        );

        verify(listener, times(3)).run();
    }

    @Test
    void shouldNotTransferIfThereAreNoResourcesInSource() {
        // Arrange
        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act & assert
        sut.doWork();
        verify(listener, never()).run();
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAvailableInSameCycle(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(C, 8, Action.EXECUTE, Actor.EMPTY);
        storage.insert(D, 9, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B, C, D));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(D, 9)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(C, 8)
        );

        sut.doWork();

        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(C, 8),
            new ResourceAmount(D, 9)
        );

        sut.doWork();

        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(C, 8),
            new ResourceAmount(D, 9)
        );

        storage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 2, Action.EXECUTE, Actor.EMPTY);

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 2)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 1),
            new ResourceAmount(C, 8),
            new ResourceAmount(D, 9)
        );

        sut.doWork();

        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 1),
            new ResourceAmount(B, 2),
            new ResourceAmount(C, 8),
            new ResourceAmount(D, 9)
        );

        sut.doWork();

        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 1),
            new ResourceAmount(B, 2),
            new ResourceAmount(C, 8),
            new ResourceAmount(D, 9)
        );
    }

    @Test
    void shouldResetRoundRobinStateAfterChangingFilters(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 100, Action.EXECUTE, Actor.EMPTY);
        storage.insert(C, 100, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 5);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B, C));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 95),
            new ResourceAmount(B, 100),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 95),
            new ResourceAmount(B, 95),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 5)
        );

        // Now C would be the next one, but we expect to go back to A.
        sut.setFilters(List.of(A, C));
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 90),
            new ResourceAmount(B, 95),
            new ResourceAmount(C, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 5)
        );
    }
}
