package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.support.amount.AbstractSingleAmountScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RegulatorUpgradeScreen extends AbstractSingleAmountScreen<RegulatorUpgradeContainerMenu> {
    public RegulatorUpgradeScreen(final RegulatorUpgradeContainerMenu menu,
                                  final Inventory playerInventory,
                                  final Component title) {
        super(menu, playerInventory, title, menu.getAmount(), 1);
    }
}
