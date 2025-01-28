package com.refinedmods.refinedstorage.api.grid.watcher;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static com.refinedmods.refinedstorage.api.grid.TestResource.A;
import static com.refinedmods.refinedstorage.api.grid.TestResource.B;
import static com.refinedmods.refinedstorage.api.grid.TestResource.C;
import static com.refinedmods.refinedstorage.api.grid.TestResource.D;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class GridWatcherManagerImplTest {
    GridWatcherManager sut;
    RootStorage rootStorage;

    @BeforeEach
    void setUp() {
        sut = new GridWatcherManagerImpl();
        rootStorage = new RootStorageImpl();
        rootStorage.addSource(new StorageImpl());
    }

    @Test
    void shouldAddWatcherAndNotifyOfChanges() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);

        // Act
        sut.addWatcher(watcher, ActorFixture.class, rootStorage);
        rootStorage.insert(B, 5, Action.EXECUTE, ActorFixture.INSTANCE);

        // Assert
        verify(watcher, times(1)).onChanged(B, 5, null);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldNotAddDuplicateWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        sut.addWatcher(watcher, ActorFixture.class, rootStorage);

        // Act & assert
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.addWatcher(watcher, ActorFixture.class, rootStorage),
            "Watcher is already registered"
        );
    }

    @Test
    void shouldRemoveWatcher() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);
        sut.addWatcher(watcher, ActorFixture.class, rootStorage);

        // Act
        sut.removeWatcher(watcher, rootStorage);
        rootStorage.insert(B, 5, Action.EXECUTE, ActorFixture.INSTANCE);

        // Assert
        verifyNoInteractions(watcher);
    }

    @Test
    void shouldNotRemoveWatcherThatIsNotRegistered() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);

        // Act & assert
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.removeWatcher(watcher, rootStorage),
            "Watcher is not registered"
        );
    }

    @Test
    void shouldAddAndRemoveAndAddWatcherAgain() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);

        // Act
        sut.addWatcher(watcher, ActorFixture.class, rootStorage);
        rootStorage.insert(B, 5, Action.EXECUTE, ActorFixture.INSTANCE);
        sut.removeWatcher(watcher, rootStorage);
        rootStorage.insert(C, 4, Action.EXECUTE, ActorFixture.INSTANCE);
        sut.addWatcher(watcher, ActorFixture.class, rootStorage);
        rootStorage.insert(D, 3, Action.EXECUTE, ActorFixture.INSTANCE);

        // Assert
        verify(watcher, times(1)).onChanged(B, 5, null);
        verify(watcher, times(1)).onChanged(D, 3, null);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldDetachAll() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);
        sut.addWatcher(watcher, ActorFixture.class, rootStorage);

        // Act
        sut.detachAll(rootStorage);
        rootStorage.insert(B, 10, Action.EXECUTE, ActorFixture.INSTANCE);
        assertThrows(IllegalArgumentException.class, () -> sut.addWatcher(
            watcher,
            ActorFixture.class,
            rootStorage
        ), "Watcher is already registered");

        // Assert
        verifyNoInteractions(watcher);
    }

    @Test
    void shouldAttachAll() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        rootStorage.insert(A, 10, Action.EXECUTE, ActorFixture.INSTANCE);
        sut.addWatcher(watcher, ActorFixture.class, rootStorage);
        sut.detachAll(rootStorage);
        rootStorage.insert(B, 5, Action.EXECUTE, ActorFixture.INSTANCE);

        // Act
        sut.attachAll(rootStorage);
        rootStorage.insert(C, 4, Action.EXECUTE, ActorFixture.INSTANCE);

        // Assert
        final InOrder inOrder = inOrder(watcher);
        inOrder.verify(watcher, times(1)).invalidate();
        verify(watcher, times(1)).onChanged(A, 10, null);
        verify(watcher, times(1)).onChanged(B, 5, null);
        verify(watcher, times(1)).onChanged(C, 4, null);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    void shouldNotifyAboutActivenessChange() {
        // Arrange
        final GridWatcher watcher = mock(GridWatcher.class);
        sut.activeChanged(true);
        sut.addWatcher(watcher, ActorFixture.class, rootStorage);

        // Act
        sut.activeChanged(false);
        sut.activeChanged(true);

        // Assert
        final InOrder inOrder = inOrder(watcher);
        inOrder.verify(watcher, times(1)).onActiveChanged(false);
        inOrder.verify(watcher, times(1)).onActiveChanged(true);
        inOrder.verifyNoMoreInteractions();
    }

    private static class ActorFixture implements Actor {
        public static final ActorFixture INSTANCE = new ActorFixture();

        private ActorFixture() {
        }

        @Override
        public String getName() {
            return "Fake";
        }
    }
}
