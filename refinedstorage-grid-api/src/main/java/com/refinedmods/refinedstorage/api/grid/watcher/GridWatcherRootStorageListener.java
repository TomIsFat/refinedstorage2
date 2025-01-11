package com.refinedmods.refinedstorage.api.grid.watcher;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageListener;

class GridWatcherRootStorageListener implements RootStorageListener {
    private final GridWatcher watcher;
    private final RootStorage rootStorage;
    private final Class<? extends Actor> actorType;

    GridWatcherRootStorageListener(final GridWatcher watcher,
                                   final RootStorage rootStorage,
                                   final Class<? extends Actor> actorType) {
        this.watcher = watcher;
        this.rootStorage = rootStorage;
        this.actorType = actorType;
    }

    @Override
    public InterceptResult beforeInsert(final ResourceKey resource, final long amount, final Actor actor) {
        return InterceptResult.EMPTY;
    }

    @Override
    public void changed(final MutableResourceList.OperationResult change) {
        watcher.onChanged(
            change.resource(),
            change.change(),
            rootStorage.findTrackedResourceByActorType(change.resource(), actorType).orElse(null)
        );
    }
}
