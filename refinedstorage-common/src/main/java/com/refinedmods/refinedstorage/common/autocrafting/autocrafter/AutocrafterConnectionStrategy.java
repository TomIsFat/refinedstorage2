package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.common.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.common.support.network.ColoredConnectionStrategy;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

class AutocrafterConnectionStrategy extends ColoredConnectionStrategy {
    AutocrafterConnectionStrategy(final Supplier<BlockState> blockStateProvider, final BlockPos origin) {
        super(blockStateProvider, origin);
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        for (final Direction direction : Direction.values()) {
            sink.tryConnectInSameDimension(origin.relative(direction), direction.getOpposite());
        }
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        return colorsAllowConnecting(connectingState);
    }
}
