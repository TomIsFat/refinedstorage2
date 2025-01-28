package com.refinedmods.refinedstorage.api.network.storage;

import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import org.apiguardian.api.API;

/**
 * Implement this on {@link com.refinedmods.refinedstorage.api.network.node.NetworkNode}s that can provide a storage
 * to the network.
 * Never modify a {@link RootStorage}
 * from a {@link com.refinedmods.refinedstorage.api.network.node.NetworkNode} directly.
 * Use this interface to help you manage the lifecycle of your storage, to ensure that your storage is added or removed
 * in the right cases.
 * Use a {@link com.refinedmods.refinedstorage.api.storage.composite.CompositeStorage} to conditionally enable/disable
 * a provided {@link Storage}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
@FunctionalInterface
public interface StorageProvider {
    /**
     * This method is called when a {@link com.refinedmods.refinedstorage.api.network.node.NetworkNode} is added or
     * removed from a network.
     *
     * @return the storage
     */
    Storage getStorage();
}
