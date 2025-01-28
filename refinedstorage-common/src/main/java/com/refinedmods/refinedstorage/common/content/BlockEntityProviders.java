package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.constructordestructor.AbstractConstructorBlockEntity;
import com.refinedmods.refinedstorage.common.constructordestructor.AbstractDestructorBlockEntity;
import com.refinedmods.refinedstorage.common.exporter.AbstractExporterBlockEntity;
import com.refinedmods.refinedstorage.common.importer.AbstractImporterBlockEntity;
import com.refinedmods.refinedstorage.common.networking.AbstractCableBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.storage.externalstorage.AbstractExternalStorageBlockEntity;
import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntity;

public record BlockEntityProviders(
    BlockEntityProvider<AbstractDiskDriveBlockEntity> diskDrive,
    BlockEntityProvider<AbstractPortableGridBlockEntity> portableGrid,
    BlockEntityProvider<AbstractPortableGridBlockEntity> creativePortableGrid,
    BlockEntityProvider<AbstractDiskInterfaceBlockEntity> diskInterface,
    BlockEntityProvider<AbstractCableBlockEntity> cable,
    BlockEntityProvider<AbstractExternalStorageBlockEntity> externalStorage,
    BlockEntityProvider<AbstractExporterBlockEntity> exporter,
    BlockEntityProvider<AbstractImporterBlockEntity> importer,
    BlockEntityProvider<AbstractConstructorBlockEntity> constructor,
    BlockEntityProvider<AbstractDestructorBlockEntity> destructor
) {
}
