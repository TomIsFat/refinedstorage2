package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;

import net.minecraft.world.entity.player.Inventory;

public class CraftingGridContainerMenu extends AbstractCraftingGridContainerMenu {
    public CraftingGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final GridData gridData) {
        super(Menus.INSTANCE.getCraftingGrid(), syncId, playerInventory, gridData);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        resized(0, 0, 0);
    }

    CraftingGridContainerMenu(final int syncId,
                              final Inventory playerInventory,
                              final CraftingGridBlockEntity craftingGrid) {
        super(Menus.INSTANCE.getCraftingGrid(), syncId, playerInventory, craftingGrid);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            craftingGrid::getRedstoneMode,
            craftingGrid::setRedstoneMode
        ));
        resized(0, 0, 0);
    }
}
