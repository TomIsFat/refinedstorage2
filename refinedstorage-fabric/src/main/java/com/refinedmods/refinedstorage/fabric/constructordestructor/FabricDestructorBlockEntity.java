package com.refinedmods.refinedstorage.fabric.constructordestructor;

import com.refinedmods.refinedstorage.common.constructordestructor.AbstractDestructorBlockEntity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricDestructorBlockEntity extends AbstractDestructorBlockEntity {
    public FabricDestructorBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return connections;
    }
}
