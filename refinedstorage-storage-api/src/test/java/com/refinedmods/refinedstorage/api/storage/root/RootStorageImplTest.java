package com.refinedmods.refinedstorage.api.storage.root;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.composite.PriorityStorage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static com.refinedmods.refinedstorage.api.storage.TestResource.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RootStorageImplTest {
    private RootStorage sut;

    @BeforeEach
    void setUp() {
        sut = new RootStorageImpl();
    }

    @Test
    void shouldAddSource() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 8, Action.EXECUTE, Actor.EMPTY);

        // Act
        sut.addSource(storage);

        final long inserted = sut.insert(A, 3, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(inserted).isEqualTo(2);
    }

    @Test
    void shouldRemoveSource() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);

        final Storage removedStorage = new LimitedStorageImpl(10);
        removedStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);
        sut.addSource(removedStorage);

        // Act
        sut.removeSource(removedStorage);

        final long extracted = sut.extract(A, 15, Action.SIMULATE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5)
        );
        assertThat(extracted).isEqualTo(5);
    }

    @Test
    void shouldFindMatchingStorage() {
        // Arrange
        final Storage matchedStorage = new LimitedStorageImpl(10);
        matchedStorage.insert(A, 8, Action.EXECUTE, Actor.EMPTY);
        sut.addSource(matchedStorage);

        final Storage unmatchedStorage = new LimitedStorageImpl(10);

        // Act
        final boolean foundMatched = sut.hasSource(s -> s == matchedStorage);
        final boolean foundUnmatched = sut.hasSource(s -> s == unmatchedStorage);

        // Assert
        assertThat(foundMatched).isTrue();
        assertThat(foundUnmatched).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallListenerOnInsertion(final Action action) {
        // Arrange
        sut.addSource(new LimitedStorageImpl(10));
        sut.insert(A, 2, Action.EXECUTE, Actor.EMPTY);

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);

        final var changedResource = ArgumentCaptor.forClass(MutableResourceList.OperationResult.class);

        // Act
        sut.insert(A, 8, action, Actor.EMPTY);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).changed(changedResource.capture());

            assertThat(changedResource.getValue().change()).isEqualTo(8);
            assertThat(changedResource.getValue().resource()).isEqualTo(A);
            assertThat(changedResource.getValue().amount()).isEqualTo(10);
        } else {
            verify(listener, never()).changed(any());
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallListenerOnExtraction(final Action action) {
        // Arrange
        final Storage storage = new LimitedStorageImpl(10);
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);
        sut.extract(A, 2, Action.EXECUTE, Actor.EMPTY);

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);

        final var changedResource = ArgumentCaptor.forClass(MutableResourceList.OperationResult.class);

        // Act
        sut.extract(A, 5, action, Actor.EMPTY);

        // Assert
        if (action == Action.EXECUTE) {
            verify(listener, atMost(1)).changed(changedResource.capture());

            assertThat(changedResource.getValue().change()).isEqualTo(-5);
            assertThat(changedResource.getValue().resource()).isEqualTo(A);
            assertThat(changedResource.getValue().amount()).isEqualTo(3);
        } else {
            verify(listener, never()).changed(any());
        }
    }

    @Test
    void shouldRemoveListener() {
        // Arrange
        sut.addSource(new LimitedStorageImpl(10));
        sut.insert(A, 2, Action.EXECUTE, Actor.EMPTY);

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);

        // Act
        sut.removeListener(listener);
        sut.insert(A, 8, Action.EXECUTE, Actor.EMPTY);

        // Assert
        verify(listener, never()).changed(any());
    }

    @Test
    void shouldInsert() {
        // Arrange
        sut.addSource(new LimitedStorageImpl(10));

        // Act
        final long inserted1 = sut.insert(A, 5, Action.EXECUTE, Actor.EMPTY);
        final long inserted2 = sut.insert(B, 4, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 5),
            new ResourceAmount(B, 4)
        );
        assertThat(inserted1).isEqualTo(5);
        assertThat(inserted2).isEqualTo(4);
        assertThat(sut.getStored()).isEqualTo(9);
    }

    @Test
    void shouldDetectWhenInterceptorIsIndicatingItInterceptedMoreThanOriginallyProvided() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener = mock(RootStorageListener.class, "listener mock");
        sut.addListener(listener);
        when(listener.beforeInsert(A, 10, Actor.EMPTY)).thenReturn(11L);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalStateException.class).hasMessage(
            "Listener listener mock indicated it intercepted 11 while the original amount to be intercepted was 10"
        );
    }

    @Test
    void shouldDetectWhenInterceptorIsIndicatingItInterceptedANegativeAmount() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener = mock(RootStorageListener.class, "listener mock");
        sut.addListener(listener);
        when(listener.beforeInsert(A, 10, Actor.EMPTY)).thenReturn(-1L);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalStateException.class).hasMessage(
            "Listener listener mock indicated it intercepted -1 while the original amount to be intercepted was 10"
        );
    }

    @Test
    void shouldNotModifyInsertionAmountWhenSimulating() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);

        // Act
        final long inserted = sut.insert(A, 10, Action.SIMULATE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getStored()).isZero();
        verify(listener, never()).beforeInsert(A, 10, Actor.EMPTY);
    }

    @Test
    void shouldModifyInsertionAmountPartiallyWithSingleListener() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);
        when(listener.beforeInsert(A, 10, Actor.EMPTY)).thenReturn(7L);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getStored()).isEqualTo(3);
        verify(listener).beforeInsert(A, 10, Actor.EMPTY);
    }

    @Test
    void shouldNotModifyInsertionAmountPartiallyWithSingleListenerWhenInterceptedNothing() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);
        when(listener.beforeInsert(A, 10, Actor.EMPTY)).thenReturn(0L);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getStored()).isEqualTo(10);
        verify(listener).beforeInsert(A, 10, Actor.EMPTY);
    }

    @Test
    void shouldModifyInsertionAmountPartiallyWithMultipleListeners() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener1 = mock(RootStorageListener.class);
        sut.addListener(listener1);
        when(listener1.beforeInsert(A, 10, Actor.EMPTY)).thenReturn(7L);

        final RootStorageListener listener2 = mock(RootStorageListener.class);
        sut.addListener(listener2);
        when(listener2.beforeInsert(A, 3, Actor.EMPTY)).thenReturn(1L);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 2)
        );
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getStored()).isEqualTo(2);
        verify(listener1).beforeInsert(A, 10, Actor.EMPTY);
        verify(listener2).beforeInsert(A, 3, Actor.EMPTY);
    }

    @Test
    void shouldModifyInsertionAmountCompletelyWithMultipleListeners() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener1 = mock(RootStorageListener.class);
        sut.addListener(listener1);
        when(listener1.beforeInsert(A, 10, Actor.EMPTY)).thenReturn(7L);

        final RootStorageListener listener2 = mock(RootStorageListener.class);
        sut.addListener(listener2);
        when(listener2.beforeInsert(A, 3, Actor.EMPTY)).thenReturn(3L);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getStored()).isZero();
        verify(listener1).beforeInsert(A, 10, Actor.EMPTY);
        verify(listener2).beforeInsert(A, 3, Actor.EMPTY);
    }

    @Test
    void shouldModifyInsertionAmountCompletelyWithSingleListener() {
        // Arrange
        sut.addSource(new StorageImpl());

        final RootStorageListener listener = mock(RootStorageListener.class);
        sut.addListener(listener);
        when(listener.beforeInsert(A, 10, Actor.EMPTY)).thenReturn(10L);

        // Act
        final long inserted = sut.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).isEmpty();
        assertThat(inserted).isEqualTo(10);
        assertThat(sut.getStored()).isZero();
        verify(listener).beforeInsert(A, 10, Actor.EMPTY);
    }

    @Test
    void shouldExtract() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act
        final long extracted = sut.extract(A, 49, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
        assertThat(extracted).isEqualTo(49);
        assertThat(sut.getStored()).isEqualTo(1);
    }

    @Test
    void shouldRetrieveIfResourceIsContained() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act & assert
        assertThat(sut.contains(A)).isTrue();
        assertThat(sut.contains(B)).isFalse();
    }

    @Test
    void shouldRetrieveResourceAmount() {
        // Arrange
        final Storage storage = new LimitedStorageImpl(100);
        storage.insert(A, 50, Action.EXECUTE, Actor.EMPTY);
        storage.extract(A, 25, Action.EXECUTE, Actor.EMPTY);

        sut.addSource(storage);

        // Act & assert
        assertThat(sut.get(A)).isEqualTo(25);
        assertThat(sut.get(B)).isZero();
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void shouldRetrieveTrackedResource() {
        // Arrange
        final Storage storage = new TrackedStorageImpl(
            new LimitedStorageImpl(100),
            () -> 0L
        );

        sut.addSource(storage);

        // Act
        sut.insert(A, 50, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(sut.contains(A)).isTrue();
        assertThat(sut.findTrackedResourceByActorType(A, Actor.EMPTY.getClass()))
            .get()
            .usingRecursiveComparison()
            .isEqualTo(new TrackedResource("Empty", 0));
    }

    @Test
    void shouldSortSources() {
        // Arrange
        final PriorityStorage storage1 = PriorityStorage.of(new LimitedStorageImpl(10), 0, 0);
        final PriorityStorage storage2 = PriorityStorage.of(new LimitedStorageImpl(10), 0, 0);
        final PriorityStorage storage3 = PriorityStorage.of(new LimitedStorageImpl(10), 0, 0);

        sut.addSource(storage1);
        sut.addSource(storage2);
        sut.addSource(storage3);

        storage1.setInsertPriority(8);
        storage2.setInsertPriority(15);
        storage3.setInsertPriority(2);

        storage1.setExtractPriority(8);
        storage2.setExtractPriority(2);
        storage3.setExtractPriority(15);

        // Act & assert
        sut.sortSources();

        sut.insert(A, 15, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 5)
        );
        assertThat(storage3.getAll()).isEmpty();

        sut.extract(A, 12, Action.EXECUTE, Actor.EMPTY);
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 3)
        );
        assertThat(storage1.getAll()).isEmpty();
        assertThat(storage3.getAll()).isEmpty();
    }
}
