package com.refinedmods.refinedstorage.api.storage.limited;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;

import org.apiguardian.api.API;

/**
 * This class can decorate any other {@link Storage} to add a capacity to it.
 * {@link InsertableStorage#insert(com.refinedmods.refinedstorage.api.resource.ResourceKey, long, Action, Actor)}
 * operations will respect this capacity.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class LimitedStorageImpl extends AbstractProxyStorage implements LimitedStorage {
    private final long capacity;

    /**
     * @param delegate the storage that is being decorated
     * @param capacity the capacity, must be 0 or larger than 0
     */
    public LimitedStorageImpl(final Storage delegate, final long capacity) {
        super(delegate);
        this.capacity = CoreValidations.validateNotNegative(capacity, "Capacity cannot be negative");
    }

    /**
     * Constructs the limited storage with a {@link StorageImpl} storage.
     *
     * @param capacity the capacity, must be 0 or larger than 0
     */
    public LimitedStorageImpl(final long capacity) {
        this(new StorageImpl(), capacity);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long spaceRemaining = capacity - delegate.getStored();
        if (spaceRemaining == 0) {
            return 0;
        }
        final long maxInsert = Math.min(amount, spaceRemaining);
        return super.insert(resource, maxInsert, action, actor);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }
}
