package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemPlayerValidator;
import com.refinedmods.refinedstorage.common.support.network.InWorldNetworkNodeContainerImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

class WirelessTransmitterNetworkNodeContainer extends InWorldNetworkNodeContainerImpl
    implements NetworkItemPlayerValidator {
    private final WirelessTransmitterBlockEntity blockEntity;
    private final AbstractNetworkNode node;

    WirelessTransmitterNetworkNodeContainer(final WirelessTransmitterBlockEntity blockEntity,
                                            final AbstractNetworkNode node,
                                            final String name,
                                            final WirelessTransmitterConnectionStrategy connectionStrategy) {
        super(blockEntity, node, name, 0, connectionStrategy, null);
        this.blockEntity = blockEntity;
        this.node = node;
    }

    @Override
    public boolean isValid(final PlayerCoordinates coordinates) {
        final Level level = blockEntity.getLevel();
        if (level == null || level.dimension() != coordinates.dimension()) {
            return false;
        }
        if (!node.isActive()) {
            return false;
        }
        final BlockPos pos = blockEntity.getBlockPos();
        final Vec3 playerPos = coordinates.position();
        final double distance = Math.sqrt(
            Math.pow(pos.getX() - playerPos.x(), 2)
                + Math.pow(pos.getY() - playerPos.y(), 2)
                + Math.pow(pos.getZ() - playerPos.z(), 2)
        );
        return distance <= blockEntity.getRange();
    }
}
