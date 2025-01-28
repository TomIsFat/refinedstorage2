package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.DisabledResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;

import net.minecraft.network.chat.Component;

class AlternativeSlot extends DisabledResourceSlot {
    private boolean active = true;

    AlternativeSlot(
        final ResourceContainer container,
        final int index,
        final int x,
        final int y
    ) {
        super(container, index, Component.empty(), x, y, ResourceSlotType.FILTER);
    }

    @Override
    public boolean isHighlightable() {
        return false; // we render the highlight in the scissor render
    }

    @Override
    public boolean isActive() {
        return active;
    }

    void setActive(final boolean active) {
        this.active = active;
    }
}
