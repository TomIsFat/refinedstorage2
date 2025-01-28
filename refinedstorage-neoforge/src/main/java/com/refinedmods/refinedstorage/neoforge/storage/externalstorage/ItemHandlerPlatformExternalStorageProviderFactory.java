package com.refinedmods.refinedstorage.neoforge.storage.externalstorage;

import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.common.api.storage.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCache;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCacheImpl;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class ItemHandlerPlatformExternalStorageProviderFactory implements PlatformExternalStorageProviderFactory {
    @Override
    public Optional<ExternalStorageProvider> create(final ServerLevel level,
                                                    final BlockPos pos,
                                                    final Direction direction) {
        final CapabilityCache capabilityCache = new CapabilityCacheImpl(level, pos, direction);
        return capabilityCache.getItemHandler().map(handler -> new ItemHandlerExternalStorageProvider(capabilityCache));
    }
}
