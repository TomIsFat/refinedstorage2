package com.refinedmods.refinedstorage.api.grid.watcher;

import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageListener;

import javax.annotation.Nullable;

class GridWatcherRegistration {
    private final GridWatcher watcher;
    private final Class<? extends Actor> actorType;
    @Nullable
    private RootStorageListener listener;

    GridWatcherRegistration(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        this.watcher = watcher;
        this.actorType = actorType;
    }

    void attach(final RootStorage rootStorage, final boolean replay) {
        this.listener = new GridWatcherRootStorageListener(watcher, rootStorage, actorType);
        rootStorage.addListener(listener);
        if (replay) {
            rootStorage.getAll().forEach(resourceAmount -> watcher.onChanged(
                resourceAmount.resource(),
                resourceAmount.amount(),
                rootStorage.findTrackedResourceByActorType(
                    resourceAmount.resource(),
                    actorType
                ).orElse(null)
            ));
        }
    }

    void detach(final RootStorage rootStorage) {
        if (listener == null) {
            return;
        }
        rootStorage.removeListener(listener);
        listener = null;
    }
}
