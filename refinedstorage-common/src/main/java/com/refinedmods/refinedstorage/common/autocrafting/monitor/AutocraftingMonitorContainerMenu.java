package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class AutocraftingMonitorContainerMenu extends AbstractAutocraftingMonitorContainerMenu {
    public AutocraftingMonitorContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final AutocraftingMonitorData data) {
        super(Menus.INSTANCE.getAutocraftingMonitor(), syncId, playerInventory, data);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    AutocraftingMonitorContainerMenu(final int syncId,
                                     final Player player,
                                     final AutocraftingMonitorBlockEntity autocraftingMonitor) {
        super(Menus.INSTANCE.getAutocraftingMonitor(), syncId, player, autocraftingMonitor);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            autocraftingMonitor::getRedstoneMode,
            autocraftingMonitor::setRedstoneMode
        ));
    }
}
