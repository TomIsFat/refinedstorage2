package com.refinedmods.refinedstorage.fabric.storage.externalstorage;

import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.common.api.storage.externalstorage.PlatformExternalStorageProviderFactory;

import java.util.Optional;
import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStoragePlatformExternalStorageProviderFactory<T>
    implements PlatformExternalStorageProviderFactory {
    private final BlockApiLookup<Storage<T>, Direction> lookup;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final Function<ResourceKey, @NullableType T> toPlatformMapper;
    private final int priority;

    public FabricStoragePlatformExternalStorageProviderFactory(final BlockApiLookup<Storage<T>, Direction> lookup,
                                                               final Function<T, ResourceKey> fromPlatformMapper,
                                                               @NullableType final Function<ResourceKey, T>
                                                                   toPlatformMapper,
                                                               final int priority) {
        this.lookup = lookup;
        this.fromPlatformMapper = fromPlatformMapper;
        this.toPlatformMapper = toPlatformMapper;
        this.priority = priority;
    }

    @Override
    public Optional<ExternalStorageProvider> create(final ServerLevel level,
                                                    final BlockPos pos,
                                                    final Direction direction) {
        if (lookup.find(level, pos, direction) == null) {
            return Optional.empty();
        }
        return Optional.of(new FabricStorageExternalStorageProvider<>(
            lookup,
            fromPlatformMapper,
            toPlatformMapper,
            level,
            pos,
            direction
        ));
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
