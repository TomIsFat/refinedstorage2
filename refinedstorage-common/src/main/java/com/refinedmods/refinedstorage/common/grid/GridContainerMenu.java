package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;

import net.minecraft.world.entity.player.Inventory;

public class GridContainerMenu extends AbstractGridContainerMenu {
    public GridContainerMenu(final int syncId, final Inventory playerInventory, final GridData gridData) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, gridData);
        resized(0, 0, 0);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    GridContainerMenu(final int syncId, final Inventory playerInventory, final GridBlockEntity grid) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, grid);
        resized(0, 0, 0);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            grid::getRedstoneMode,
            grid::setRedstoneMode
        ));
    }
}
