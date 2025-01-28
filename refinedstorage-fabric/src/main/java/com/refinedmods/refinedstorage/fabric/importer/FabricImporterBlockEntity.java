package com.refinedmods.refinedstorage.fabric.importer;

import com.refinedmods.refinedstorage.common.importer.AbstractImporterBlockEntity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricImporterBlockEntity extends AbstractImporterBlockEntity {
    public FabricImporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return connections;
    }
}
