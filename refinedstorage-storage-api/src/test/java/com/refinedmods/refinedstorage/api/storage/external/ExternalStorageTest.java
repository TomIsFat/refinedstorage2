package com.refinedmods.refinedstorage.api.storage.external;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;

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

class ExternalStorageTest {
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

        // Act
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldTakeExistingResourcesIntoConsiderationWhenDetectingChanges() {
        // Arrange
        final Storage storage = new TransformingStorage();
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        // Act
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A_TRANSFORMED, 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectCompletelyNewResource() {
        // Arrange
        final Storage storage = new TransformingStorage();
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        // Act
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A_TRANSFORMED, 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectAdditionToExistingResource() {
        // Arrange
        final Storage storage = new TransformingStorage();
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        // Act
        storage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A_TRANSFORMED, 11)
        );
        assertThat(sut.getStored()).isEqualTo(11);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }


    @Test
    void shouldDetectPartialRemovalOfExistingResource() {
        // Arrange
        final Storage storage = new TransformingStorage();
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        // Act
        storage.extract(A_TRANSFORMED, 2, Action.EXECUTE, Actor.EMPTY);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A_TRANSFORMED, 8)
        );
        assertThat(sut.getStored()).isEqualTo(8);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectCompleteRemovalOfExistingResource() {
        // Arrange
        final Storage storage = new TransformingStorage();
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        // Act
        storage.extract(A_TRANSFORMED, 10, Action.EXECUTE, Actor.EMPTY);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectCompleteRemovalOfExistingResourceAndAdditionOfNewResource() {
        // Arrange
        final Storage storage = new TransformingStorage();
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        // Act
        storage.extract(A_TRANSFORMED, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B_TRANSFORMED, 10)
        );
        assertThat(sut.getStored()).isEqualTo(10);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldDetectAdditionOfExistingResourceAndAdditionOfNewResource() {
        // Arrange
        final Storage storage = new TransformingStorage();
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        // Act
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isTrue();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A_TRANSFORMED, 20),
            new ResourceAmount(B_TRANSFORMED, 1)
        );
        assertThat(sut.getStored()).isEqualTo(21);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @Test
    void shouldNotDetectAnyChangesWhenNoChangesAreMade() {
        // Arrange
        final Storage storage = new TransformingStorage();
        final ExternalStorage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        storage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);
        storage.extract(A_TRANSFORMED, 5, Action.EXECUTE, Actor.EMPTY);
        storage.extract(B_TRANSFORMED, 10, Action.EXECUTE, Actor.EMPTY);
        sut.detectChanges();

        // Act
        final boolean hasChanges = sut.detectChanges();

        // Assert
        assertThat(hasChanges).isFalse();
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A_TRANSFORMED, 15)
        );
        assertThat(sut.getStored()).isEqualTo(15);
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertAndDetectChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        // Act
        final long insertedA1 = sut.insert(A, 10, action, Actor.EMPTY);
        final long insertedA2 = sut.insert(A, 1, action, Actor.EMPTY);
        final long insertedB = sut.insert(B, 5, action, Actor.EMPTY);

        // Assert
        assertThat(insertedA1).isEqualTo(10);
        assertThat(insertedA2).isEqualTo(1);
        assertThat(insertedB).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 11),
                new ResourceAmount(B_TRANSFORMED, 5)
            );
            assertThat(sut.getStored()).isEqualTo(16);
            assertThat(listener.resources).containsExactly(A, A, B);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        } else {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
            assertThat(listener.resources).isEmpty();
            assertThat(listener.actors).isEmpty();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotCallListenerWhenInsertionFailed(final Action action) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(0);
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        // Act
        final long extracted = sut.insert(A, 1, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldPartiallyExtractAndDetectChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.insert(A_ALTERNATIVE, 10, Action.EXECUTE, Actor.EMPTY);
        sut.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        // this will try to extract A!(5) and A2!(5/2)
        final long extracted = sut.extract(A_TRANSFORMED, 5, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(5);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 5),
                new ResourceAmount(A_ALTERNATIVE, 8),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(sut.getStored()).isEqualTo(23);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B, A_TRANSFORMED);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 10),
                new ResourceAmount(A_ALTERNATIVE, 10),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(sut.getStored()).isEqualTo(30);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCompletelyExtractAndDetectChanges(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);
        sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        sut.insert(A_ALTERNATIVE, 10, Action.EXECUTE, Actor.EMPTY);
        sut.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        // this will try to extract A!(10) and A2!(10/2)
        final long extracted = sut.extract(A_TRANSFORMED, 10, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_ALTERNATIVE, 5),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(sut.getStored()).isEqualTo(15);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B, A_TRANSFORMED);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A_TRANSFORMED, 10),
                new ResourceAmount(A_ALTERNATIVE, 10),
                new ResourceAmount(B_TRANSFORMED, 10)
            );
            assertThat(sut.getStored()).isEqualTo(30);
            assertThat(listener.resources).containsExactly(A, A_ALTERNATIVE, B);
            assertThat(listener.actors).containsOnly(Actor.EMPTY);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotCallListenerWhenExtractionFailed(final Action action) {
        // Arrange
        final Storage storage = new TransformingStorage();
        final Storage sut = new ExternalStorage(new ExternalStorageProviderImpl(storage), listener);

        // Act
        final long extracted = sut.extract(A, 10, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getStored()).isZero();
        assertThat(listener.resources).isEmpty();
        assertThat(listener.actors).isEmpty();
    }
}
