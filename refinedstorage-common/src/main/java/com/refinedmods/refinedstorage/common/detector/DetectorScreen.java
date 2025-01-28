package com.refinedmods.refinedstorage.common.detector;

import com.refinedmods.refinedstorage.common.support.amount.AbstractSingleAmountScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.FuzzyModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DetectorScreen extends AbstractSingleAmountScreen<DetectorContainerMenu> {
    public DetectorScreen(final DetectorContainerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title, menu.getAmount(), 0);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            () -> FuzzyModeSideButtonWidget.Type.GENERIC
        ));
        addSideButton(new DetectorModeSideButtonWidget(getMenu().getProperty(DetectorPropertyTypes.MODE)));
    }
}
