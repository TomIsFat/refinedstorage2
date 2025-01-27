package com.refinedmods.refinedstorage.api.storage.root;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorage;

import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * The entry-point for various storage operations.
 * It acts as a storage, and is usually backed by a
 * {@link com.refinedmods.refinedstorage.api.storage.composite.CompositeStorage}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface RootStorage extends Storage, TrackedStorage {
    /**
     * Adds a listener.
     *
     * @param listener the listener
     */
    void addListener(RootStorageListener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener
     */
    void removeListener(RootStorageListener listener);

    /**
     * @param resource the resource
     * @return the amount stored, or zero if not stored
     */
    long get(ResourceKey resource);

    /**
     * @param resource the resource
     * @return whether the storage contains the resource
     */
    boolean contains(ResourceKey resource);

    /**
     * Sorts the sources in the backing storage.
     */
    void sortSources();

    /**
     * Adds a source and resorts all the sources.
     *
     * @param source the source
     */
    void addSource(Storage source);

    /**
     * Removes a source .
     *
     * @param source the source
     */
    void removeSource(Storage source);

    /**
     * Checks if a source is present.
     *
     * @param matcher a predicate
     * @return whether the predicate matched
     */
    boolean hasSource(Predicate<Storage> matcher);
}
