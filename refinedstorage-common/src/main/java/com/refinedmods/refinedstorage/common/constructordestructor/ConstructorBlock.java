package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ConstructorBlock extends AbstractConstructorDestructorBlock<
    ConstructorBlock, AbstractConstructorBlockEntity, BaseBlockItem
    > implements BlockItemProvider<BaseBlockItem> {
    private static final Component HELP = createTranslation("item", "constructor.help");

    private final BlockEntityProvider<AbstractConstructorBlockEntity> blockEntityProvider;

    public ConstructorBlock(final DyeColor color,
                            final MutableComponent name,
                            final BlockEntityProvider<AbstractConstructorBlockEntity> blockEntityProvider) {
        super(color, name, new NetworkNodeBlockEntityTicker<>(
            BlockEntities.INSTANCE::getConstructor,
            ACTIVE
        ));
        this.blockEntityProvider = blockEntityProvider;
    }

    @Override
    public BlockColorMap<ConstructorBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getConstructor();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return blockEntityProvider.create(pos, state);
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this, HELP);
    }
}
