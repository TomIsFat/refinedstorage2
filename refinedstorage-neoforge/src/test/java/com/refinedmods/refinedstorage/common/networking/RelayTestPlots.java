package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

import static com.refinedmods.refinedstorage.common.GameTestUtil.RSBLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.requireBlockEntity;
import static net.minecraft.core.BlockPos.ZERO;

final class RelayTestPlots {
    private RelayTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final RelayConsumer consumer) {
        helper.setBlock(ZERO.above(), RSBLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), RSBLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            RSBLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );
        final BlockPos relayPos = ZERO.above().above().above();
        helper.setBlock(relayPos, RSBLOCKS.getRelay().getDefault().rotated(Direction.UP));

        final BlockPos subnetworkPos = relayPos.above();
        helper.setBlock(subnetworkPos, RSBLOCKS.getGrid().getDefault());

        consumer.accept(
            requireBlockEntity(helper, relayPos, RelayBlockEntity.class),
            relayPos,
            subnetworkPos,
            helper.startSequence()
        );
    }
}
