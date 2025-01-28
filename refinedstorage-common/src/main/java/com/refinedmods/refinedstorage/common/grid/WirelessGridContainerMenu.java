package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.content.Menus;

import net.minecraft.world.entity.player.Inventory;

public class WirelessGridContainerMenu extends AbstractGridContainerMenu {
    public WirelessGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final WirelessGridData wirelessGridData) {
        super(Menus.INSTANCE.getWirelessGrid(), syncId, playerInventory, wirelessGridData.gridData());
        this.disabledSlot = wirelessGridData.slotReference();
        resized(0, 0, 0);
    }

    WirelessGridContainerMenu(final int syncId,
                              final Inventory playerInventory,
                              final Grid grid,
                              final SlotReference slotReference) {
        super(Menus.INSTANCE.getWirelessGrid(), syncId, playerInventory, grid);
        this.disabledSlot = slotReference;
        resized(0, 0, 0);
    }
}
