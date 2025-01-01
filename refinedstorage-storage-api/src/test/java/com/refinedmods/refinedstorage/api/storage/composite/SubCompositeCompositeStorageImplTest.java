package com.refinedmods.refinedstorage.api.storage.composite;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static com.refinedmods.refinedstorage.api.storage.TestResource.B;
import static org.assertj.core.api.Assertions.assertThat;

class SubCompositeCompositeStorageImplTest {
    private CompositeStorageImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CompositeStorageImpl(MutableResourceListImpl.create());
    }

    @Test
    void testAddingSubCompositeShouldAddAllResourcesToParent() {
        // Arrange
        final CompositeStorage subComposite = new CompositeStorageImpl(MutableResourceListImpl.create());
        subComposite.addSource(new StorageImpl());
        subComposite.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        sut.addSource(subComposite);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void testRemovingSubCompositeShouldRemoveAllResourcesFromParent() {
        // Arrange
        final CompositeStorage subComposite = new CompositeStorageImpl(MutableResourceListImpl.create());
        subComposite.addSource(new StorageImpl());
        subComposite.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(subComposite);

        final Storage subCompositeStorage = new StorageImpl();
        subCompositeStorage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        sut.removeSource(subComposite);

        subComposite.addSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).isEmpty();
    }

    @Test
    void testAddingSourceToSubCompositeShouldNotifyParent() {
        // Arrange
        final CompositeStorage subComposite = new CompositeStorageImpl(MutableResourceListImpl.create());
        final Storage subStorage = new StorageImpl();
        subStorage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(subComposite);
        sut.addSource(subStorage);

        final Storage subCompositeStorage = new StorageImpl();
        subCompositeStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Act
        subComposite.addSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10),
            new ResourceAmount(B, 10)
        );

        assertThat(subComposite.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
    }

    @Test
    void testRemovingSourceFromSubCompositeShouldNotifyParent() {
        // Arrange
        final CompositeStorage subComposite = new CompositeStorageImpl(MutableResourceListImpl.create());
        final Storage subStorage = new StorageImpl();
        subStorage.insert(B, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(subComposite);
        sut.addSource(subStorage);

        final Storage subCompositeStorage = new StorageImpl();
        subCompositeStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        subComposite.addSource(subCompositeStorage);

        // Act
        subComposite.removeSource(subCompositeStorage);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10)
        );
        assertThat(subComposite.getAll()).isEmpty();
    }
}
