package com.refinedmods.refinedstorage.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.task.RandomSchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;

import java.util.List;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;

class RandomExporterNetworkNodeTest extends AbstractExporterNetworkNodeTest {
    @Override
    protected SchedulingMode createSchedulingMode() {
        return new RandomSchedulingMode(list -> {
            list.clear();
            list.add(sut.new ExporterTask(A));
            list.add(sut.new ExporterTask(B));
        });
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
        sut.setFilters(List.of(B, A));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 95),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );

        sut.doWork();

        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 90),
            new ResourceAmount(B, 100)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void shouldUseNextResourceIfFirstOneIsNotAvailableInSameCycle(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(B, 7, Action.EXECUTE, Actor.EMPTY);

        final Storage destination = new StorageImpl();
        final ExporterTransferStrategy strategy = createTransferStrategy(destination, 10);

        sut.setTransferStrategy(strategy);
        sut.setFilters(List.of(A, B));

        // Act & assert
        sut.doWork();

        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 7)
        );

        sut.doWork();

        assertThat(storage.getAll()).isEmpty();
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 7)
        );
    }
}
