package com.refinedmods.refinedstorage.neoforge.autocrafting;

import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternSinkFactory;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCacheImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class ItemHandlerExternalPatternProviderSinkFactory
    implements PatternProviderExternalPatternSinkFactory {
    @Override
    public PlatformPatternProviderExternalPatternSink create(final ServerLevel level,
                                                             final BlockPos pos,
                                                             final Direction direction) {
        return new ItemHandlerExternalPatternProviderSink(new CapabilityCacheImpl(level, pos, direction));
    }
}
