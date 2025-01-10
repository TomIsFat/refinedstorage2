package com.refinedmods.refinedstorage.neoforge.autocrafting;

import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternInputSink;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternInputSinkFactory;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCacheImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FluidHandlerExternalPatternProviderInputSinkFactory
    implements PatternProviderExternalPatternInputSinkFactory {
    @Override
    public PatternProviderExternalPatternInputSink create(final ServerLevel level,
                                                          final BlockPos pos,
                                                          final Direction direction) {
        return new FluidHandlerExternalPatternProviderInputSink(new CapabilityCacheImpl(level, pos, direction));
    }
}
