package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.grid.PortableGridData;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;

import net.minecraft.world.entity.player.Inventory;

public class PortableGridBlockContainerMenu extends AbstractPortableGridContainerMenu {
    public PortableGridBlockContainerMenu(final int syncId,
                                          final Inventory playerInventory,
                                          final PortableGridData portableGridData) {
        super(Menus.INSTANCE.getPortableGridBlock(), syncId, playerInventory, portableGridData);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        resized(0, 0, 0);
    }

    PortableGridBlockContainerMenu(final int syncId,
                                   final Inventory playerInventory,
                                   final AbstractPortableGridBlockEntity portableGrid) {
        super(
            Menus.INSTANCE.getPortableGridBlock(),
            syncId,
            playerInventory,
            portableGrid.getDiskInventory(),
            portableGrid.getGrid(),
            portableGrid.getEnergyStorage()
        );
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            portableGrid::getRedstoneMode,
            portableGrid::setRedstoneMode
        ));
        resized(0, 0, 0);
    }
}
