package com.refinedmods.refinedstorage.common.exporter;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.RSBLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.requireBlockEntity;
import static net.minecraft.core.BlockPos.ZERO;

final class ExporterTestPlots {
    private ExporterTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final Block block,
                            final Direction direction,
                            final TriConsumer<AbstractExporterBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), RSBLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), RSBLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            RSBLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );
        final BlockPos exporterPos = ZERO.above().above().above();
        helper.setBlock(exporterPos, RSBLOCKS.getExporter().getDefault().rotated(direction));
        helper.setBlock(exporterPos.east(), block);
        consumer.accept(
            requireBlockEntity(helper, exporterPos, AbstractExporterBlockEntity.class),
            exporterPos,
            helper.startSequence()
        );
    }
}
