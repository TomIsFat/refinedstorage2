package com.refinedmods.refinedstorage.common.support.containermenu;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

public class DisabledResourceSlot extends ResourceSlot {
    public DisabledResourceSlot(
        final ResourceContainer resourceContainer,
        final int index,
        final Component helpText,
        final int x,
        final int y,
        final ResourceSlotType type
    ) {
        super(resourceContainer, index, helpText, x, y, type);
    }

    public DisabledResourceSlot(
        final ResourceContainer resourceContainer,
        final Container resourceContainerAsContainer,
        final int index,
        final Component helpText,
        final int x,
        final int y,
        final ResourceSlotType type
    ) {
        super(resourceContainer, resourceContainerAsContainer, index, helpText, x, y, type);
    }

    @Override
    public boolean canModifyAmount() {
        return false;
    }

    @Override
    public boolean shouldRenderAmount() {
        return false;
    }

    @Override
    public boolean isDisabled() {
        return true;
    }
}
