package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.RSBLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.requireBlockEntity;
import static net.minecraft.core.BlockPos.ZERO;

final class ConstructorTestPlots {
    private ConstructorTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final Direction direction,
                            final TriConsumer<AbstractConstructorBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), RSBLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), RSBLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            RSBLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );
        final BlockPos constructorPos = ZERO.above().above().above();
        helper.setBlock(constructorPos, RSBLOCKS.getConstructor().getDefault().rotated(direction));
        consumer.accept(
            requireBlockEntity(helper, constructorPos, AbstractConstructorBlockEntity.class),
            constructorPos,
            helper.startSequence()
        );
    }
}
