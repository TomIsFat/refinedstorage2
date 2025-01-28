package com.refinedmods.refinedstorage.common.content;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@FunctionalInterface
public interface BlockEntityTypeFactory {
    <T extends BlockEntity> BlockEntityType<T> create(BlockEntityProvider<T> factory, Block... allowedBlocks);
}
