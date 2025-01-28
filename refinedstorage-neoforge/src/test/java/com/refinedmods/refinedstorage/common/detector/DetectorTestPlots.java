package com.refinedmods.refinedstorage.common.detector;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.RSBLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.requireBlockEntity;
import static net.minecraft.core.BlockPos.ZERO;

final class DetectorTestPlots {
    private DetectorTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final Direction direction,
                            final TriConsumer<DetectorBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), RSBLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), RSBLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            RSBLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );
        helper.setBlock(ZERO.above().above().above().north(), Blocks.REDSTONE_WIRE);
        final BlockPos detectorPos = ZERO.above().above().above();
        helper.setBlock(detectorPos, RSBLOCKS.getDetector().getDefault().rotated(direction));
        consumer.accept(
            requireBlockEntity(helper, detectorPos, DetectorBlockEntity.class),
            detectorPos,
            helper.startSequence()
        );
    }
}
