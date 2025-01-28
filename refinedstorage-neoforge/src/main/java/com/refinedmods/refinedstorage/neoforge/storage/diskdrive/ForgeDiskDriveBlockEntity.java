package com.refinedmods.refinedstorage.neoforge.storage.diskdrive;

import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntity;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class ForgeDiskDriveBlockEntity extends AbstractDiskDriveBlockEntity {
    public static final ModelProperty<Disk[]> DISKS_PROPERTY = new ModelProperty<>();

    public ForgeDiskDriveBlockEntity(final BlockPos pos, final BlockState state) {
        super(pos, state);
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(DISKS_PROPERTY, disks).build();
    }
}
