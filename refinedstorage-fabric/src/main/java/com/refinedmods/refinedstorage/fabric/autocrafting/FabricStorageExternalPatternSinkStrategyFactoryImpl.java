package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.fabric.api.FabricStorageExternalPatternSinkStrategy;
import com.refinedmods.refinedstorage.fabric.api.FabricStorageExternalPatternSinkStrategyFactory;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStorageExternalPatternSinkStrategyFactoryImpl<T>
    implements FabricStorageExternalPatternSinkStrategyFactory {
    private final BlockApiLookup<Storage<T>, Direction> lookup;
    private final Function<ResourceKey, @NullableType T> toPlatformMapper;

    public FabricStorageExternalPatternSinkStrategyFactoryImpl(
        final BlockApiLookup<Storage<T>, Direction> lookup,
        final Function<ResourceKey, @NullableType T> toPlatformMapper
    ) {
        this.lookup = lookup;
        this.toPlatformMapper = toPlatformMapper;
    }

    @Override
    public FabricStorageExternalPatternSinkStrategy create(final ServerLevel level,
                                                           final BlockPos pos,
                                                           final Direction direction) {
        return new FabricStorageExternalPatternSinkStrategyImpl<>(
            lookup,
            toPlatformMapper,
            level,
            pos,
            direction
        );
    }
}
