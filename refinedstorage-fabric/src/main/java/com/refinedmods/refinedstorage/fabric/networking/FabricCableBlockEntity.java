package com.refinedmods.refinedstorage.fabric.networking;

import com.refinedmods.refinedstorage.common.networking.AbstractCableBlockEntity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricCableBlockEntity extends AbstractCableBlockEntity {
    public FabricCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return connections;
    }
}
