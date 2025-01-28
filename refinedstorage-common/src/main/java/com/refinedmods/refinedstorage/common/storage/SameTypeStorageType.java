package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageType;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class SameTypeStorageType<T extends ResourceKey> implements StorageType {
    private final Codec<T> codec;
    private final Predicate<ResourceKey> valid;
    private final Function<ResourceKey, T> caster;
    private final long diskInterfaceTransferQuota;
    private final long diskInterfaceTransferQuotaWithStackUpgrade;

    public SameTypeStorageType(final Codec<T> codec,
                               final Predicate<ResourceKey> valid,
                               final Function<ResourceKey, T> caster,
                               final long diskInterfaceTransferQuota,
                               final long diskInterfaceTransferQuotaWithStackUpgrade) {
        this.codec = codec;
        this.valid = valid;
        this.caster = caster;
        this.diskInterfaceTransferQuota = diskInterfaceTransferQuota;
        this.diskInterfaceTransferQuotaWithStackUpgrade = diskInterfaceTransferQuotaWithStackUpgrade;
    }

    @Override
    public SerializableStorage create(@Nullable final Long capacity, final Runnable listener) {
        return createStorage(StorageCodecs.StorageData.empty(capacity), listener);
    }

    @Override
    public MapCodec<SerializableStorage> getMapCodec(final Runnable listener) {
        return StorageCodecs.sameTypeStorageData(codec).xmap(
            storageData -> createStorage(storageData, listener),
            storage -> StorageCodecs.StorageData.ofSameTypeStorage(storage, valid, caster)
        );
    }

    @Override
    public boolean isAllowed(final ResourceKey resource) {
        return valid.test(resource);
    }

    @Override
    public long getDiskInterfaceTransferQuota(final boolean stackUpgrade) {
        if (stackUpgrade) {
            return diskInterfaceTransferQuotaWithStackUpgrade;
        }
        return diskInterfaceTransferQuota;
    }

    private SerializableStorage createStorage(final StorageCodecs.StorageData<T> data, final Runnable listener) {
        final TrackedStorageRepository trackingRepository = new InMemoryTrackedStorageRepository();
        final TrackedStorageImpl tracked = new TrackedStorageImpl(
            new StorageImpl(),
            trackingRepository,
            System::currentTimeMillis
        );
        final PlatformStorage storage = data.capacity().map(capacity -> {
            final LimitedStorageImpl limited = new LimitedStorageImpl(tracked, capacity);
            return (PlatformStorage) new LimitedPlatformStorage(limited, this, trackingRepository, listener);
        }).orElseGet(() -> new PlatformStorage(tracked, this, trackingRepository, listener));
        data.resources().forEach(storage::load);
        return storage;
    }
}
