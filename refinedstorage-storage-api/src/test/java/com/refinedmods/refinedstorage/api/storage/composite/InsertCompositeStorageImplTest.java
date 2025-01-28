package com.refinedmods.refinedstorage.api.storage.composite;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ActorCapturingStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static org.assertj.core.api.Assertions.assertThat;

class InsertCompositeStorageImplTest {
    private CompositeStorageImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl(MutableResourceListImpl.create());
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertToSingleSourceWithoutRemainder(final Action action) {
        // Arrange
        final ActorCapturingStorage storage = new ActorCapturingStorage(new LimitedStorageImpl(20));
        sut.addSource(storage);

        final Actor actor = () -> "Custom";

        // Act
        final long inserted = sut.insert(A, 10, action, actor);

        // Assert
        assertThat(inserted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(sut.getStored()).isEqualTo(10);
        } else {
            assertThat(storage.getAll()).isEmpty();
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(storage.getActors()).containsExactly(actor);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertToSingleSourceWithRemainder(final Action action) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(20);
        sut.addSource(storage);

        // Act
        final long inserted = sut.insert(A, 30, action, Actor.EMPTY);

        // Assert
        assertThat(inserted).isEqualTo(20);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 20)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 20)
            );
            assertThat(sut.getStored()).isEqualTo(20);
        } else {
            assertThat(storage.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertToMultipleSourcesWithoutRemainder(final Action action) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(5);
        final Storage storage2 = new LimitedStorageImpl(10);
        final Storage storage3 = new LimitedStorageImpl(20);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        final long inserted = sut.insert(A, 17, action, Actor.EMPTY);

        // Assert
        assertThat(inserted).isEqualTo(17);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 5)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 2)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 17)
            );
            assertThat(sut.getStored()).isEqualTo(17);
        } else {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isEmpty();
            assertThat(storage3.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertToMultipleSourcesWithRemainder(final Action action) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(5);
        final Storage storage2 = new LimitedStorageImpl(10);
        final Storage storage3 = new LimitedStorageImpl(20);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        // Act
        final long inserted = sut.insert(A, 39, action, Actor.EMPTY);

        // Assert
        assertThat(inserted).isEqualTo(35);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 5)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 20)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 35)
            );
            assertThat(sut.getStored()).isEqualTo(35);
        } else {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isEmpty();
            assertThat(storage3.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @Test
    void shouldNotInsertWithoutAnySourcesPresent() {
        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void shouldRespectPriorityWhenInserting() {
        // Arrange
        final PriorityStorage lowestPriority = PriorityStorage.of(new LimitedStorageImpl(10), 5, 10);
        final PriorityStorage highestPriority = PriorityStorage.of(new LimitedStorageImpl(10), 10, 5);

        sut.addSource(lowestPriority);
        sut.addSource(highestPriority);

        // Act
        sut.insert(A, 11, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(highestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(lowestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
    }
}
