package com.refinedmods.refinedstorage.api.storage.composite;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ActorCapturingStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TestResource;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static org.assertj.core.api.Assertions.assertThat;

class ExtractCompositeStorageImplTest {
    private CompositeStorageImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl(MutableResourceListImpl.create());
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromSingleSourcePartially(final Action action) {
        // Arrange
        final ActorCapturingStorage storage = new ActorCapturingStorage(new LimitedStorageImpl(10));
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        final Actor actor = () -> "Custom";

        // Act
        final long extracted = sut.extract(A, 3, action, actor);

        // Assert
        assertThat(extracted).isEqualTo(3);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sut.getStored()).isEqualTo(7);
        } else {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(sut.getStored()).isEqualTo(10);
        }

        assertThat(storage.getActors()).containsExactly(Actor.EMPTY, actor);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromSingleSourceCompletely(final Action action) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract(A, 10, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(sut.getStored()).isEqualTo(10);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotExtractMoreThanIsAvailableFromSingleSource(final Action action) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 4, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract(A, 7, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(4);

        if (action == Action.EXECUTE) {
            assertThat(storage.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 4)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 4)
            );
            assertThat(sut.getStored()).isEqualTo(4);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromMultipleSourcesPartially(final Action action) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(10);
        storage1.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage storage2 = new LimitedStorageImpl(5);
        storage2.insert(A, 3, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        final long extracted = sut.extract(A, 12, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(12);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
            assertThat(sut.getStored()).isEqualTo(1);
        } else {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 3)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 13)
            );
            assertThat(sut.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractFromMultipleSourcesCompletely(final Action action) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(10);
        storage1.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage storage2 = new LimitedStorageImpl(5);
        storage2.insert(A, 3, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        final long extracted = sut.extract(A, 13, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 3)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 13)
            );
            assertThat(sut.getStored()).isEqualTo(13);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotExtractMoreThanIsAvailableFromMultipleSources(final Action action) {
        // Arrange
        final Storage storage1 = new LimitedStorageImpl(10);
        storage1.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final Storage storage2 = new LimitedStorageImpl(5);
        storage2.insert(A, 3, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage1);
        sut.addSource(storage2);

        // Act
        final long extracted = sut.extract(A, 30, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(storage1.getAll()).isEmpty();
            assertThat(storage2.getAll()).isEmpty();

            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 10)
            );
            assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 3)
            );

            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 13)
            );
            assertThat(sut.getStored()).isEqualTo(13);
        }
    }

    @Test
    void shouldNotExtractWithoutResourcePresent() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract(TestResource.B, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void shouldNotExtractWithoutAnySourcesPresent() {
        // Act
        final long extracted = sut.extract(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void shouldRespectPriorityWhenExtracting() {
        // Arrange
        final PriorityStorage lowestPriority = PriorityStorage.of(new LimitedStorageImpl(10), 10, 5);
        final PriorityStorage highestPriority = PriorityStorage.of(new LimitedStorageImpl(10), 5, 10);

        lowestPriority.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        highestPriority.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(lowestPriority);
        sut.addSource(highestPriority);

        // Act
        sut.extract(A, 11, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(highestPriority.getAll()).isEmpty();
        assertThat(lowestPriority.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );
    }
}
