package com.refinedmods.refinedstorage.common.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestSequence;

@FunctionalInterface
public interface RelayConsumer {
    void accept(RelayBlockEntity relayBlockEntity,
                BlockPos pos,
                BlockPos subnetworkPos,
                GameTestSequence gameTestSequence);
}
