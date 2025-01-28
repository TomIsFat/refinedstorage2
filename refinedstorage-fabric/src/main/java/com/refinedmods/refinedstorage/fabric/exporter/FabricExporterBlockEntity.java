package com.refinedmods.refinedstorage.fabric.exporter;

import com.refinedmods.refinedstorage.common.exporter.AbstractExporterBlockEntity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricExporterBlockEntity extends AbstractExporterBlockEntity {
    public FabricExporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Override
    @Nullable
    public Object getRenderData() {
        return connections;
    }
}
