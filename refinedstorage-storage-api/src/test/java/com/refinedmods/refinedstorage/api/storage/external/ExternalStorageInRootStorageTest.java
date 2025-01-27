package com.refinedmods.refinedstorage.api.storage.external;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage.api.storage.external.ExternalTestResource.A;
import static com.refinedmods.refinedstorage.api.storage.external.ExternalTestResource.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.api.storage.external.ExternalTestResource.A_TRANSFORMED;
import static com.refinedmods.refinedstorage.api.storage.external.ExternalTestResource.B;
import static com.refinedmods.refinedstorage.api.storage.external.ExternalTestResource.B_TRANSFORMED;
import static org.assertj.core.api.Assertions.assertThat;

class ExternalStorageInRootStorageTest {
    SpyingExternalStorageListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpyingExternalStorageListener();
    }

    @Test
    void shouldNotTakeExistingResourcesIntoConsiderationWhenBuildingInitialState() {
        // Arrange
        final Storage storage = new TransformingStorage();
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        final RootStorage rootStorage = new RootStorageImpl();

        // Act
        rootStorage.addSource(sut);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(rootStorage.getAll()).isEmpty();
        assertThat(rootStorage.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldTakeExistingResourcesIntoConsiderationWhenDetectingChanges() {
        // Arrange
        final Storage storage = new TransformingStorage();
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        final RootStorage rootStorage = new RootStorageImpl();
        rootStorage.addSource(sut);

        // Act
        sut.detectChanges();

        // Assert
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A_TRANSFORMED, 10)
        );
        assertThat(rootStorage.getStored()).isEqualTo(10);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldNoLongerPropagateChangesToRootStorageWhenRemoving() {
        // Arrange
        final Storage storage = new TransformingStorage();
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        final RootStorage rootStorage = new RootStorageImpl();
        rootStorage.addSource(sut);

        // Act
        rootStorage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        rootStorage.removeSource(sut);
        final long insertedStraightIntoExternalStorage = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final long insertedIntoRootStorage = rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(insertedStraightIntoExternalStorage).isEqualTo(10);
        assertThat(insertedIntoRootStorage).isZero();
        assertThat(sut.getAll()).isNotEmpty();
        assertThat(sut.getStored()).isEqualTo(25);
        assertThat(rootStorage.getAll()).isEmpty();
        assertThat(rootStorage.getStored()).isZero();
        assertThat(listener.resources).containsExactly(A, A);
        assertThat(listener.actors).containsOnly(Actor.EMPTY);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        final RootStorage rootStorage = new RootStorageImpl();
        rootStorage.addSource(sut);

        // Act
        final long insertedA1 = rootStorage.insert(A, 10, action, Actor.EMPTY);
        final long insertedA2 = rootStorage.insert(A, 1, action, Actor.EMPTY);
        final long insertedB = rootStorage.insert(B, 5, action, Actor.EMPTY);

        // Assert
        assertThat(insertedA1).isEqualTo(10);
        assertThat(insertedA2).isEqualTo(1);
        assertThat(insertedB).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 11),
                new ResourceAmount(B_TRANSFORMED, 5)
            );
            assertThat(rootStorage.getStored()).isEqualTo(16);
            assertThat(listener.resources).containsExactly(A, A, B);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        } else {
            assertThat(rootStorage.getAll()).isEmpty();
            assertThat(rootStorage.getStored()).isZero();
            assertThat(listener.resources).isEmpty();
            assertThat(listener.actors).isEmpty();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractPartiallyAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.insert(A_ALTERNATIVE, 10, Action.EXECUTE, Actor.EMPTY);
        sut.insert(B, 10, Action.EXECUTE, Actor.EMPTY);
        final RootStorage rootStorage = new RootStorageImpl();
        rootStorage.addSource(sut);

        // Act
        // this will try to extract A!(5) and A2!(5/2)
        final long extracted = rootStorage.extract(A_TRANSFORMED, 5, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 5),
                new ResourceAmount(A_ALTERNATIVE, 8),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(rootStorage.getStored()).isEqualTo(23);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B, A_TRANSFORMED);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        } else {
            assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 10),
                new ResourceAmount(A_ALTERNATIVE, 10),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(rootStorage.getStored()).isEqualTo(30);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractCompletelyAndDetectAndPropagateChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.insert(A_ALTERNATIVE, 10, Action.EXECUTE, Actor.EMPTY);
        sut.insert(B, 10, Action.EXECUTE, Actor.EMPTY);
        final RootStorage rootStorage = new RootStorageImpl();
        rootStorage.addSource(sut);

        // Act
        // this will try to extract A!(10) and A2!(10/2)
        final long extracted = rootStorage.extract(A_TRANSFORMED, 10, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_ALTERNATIVE, 5),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(rootStorage.getStored()).isEqualTo(15);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B, A_TRANSFORMED);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        } else {
            assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 10),
                new ResourceAmount(A_ALTERNATIVE, 10),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(rootStorage.getStored()).isEqualTo(30);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        }
    }
}
