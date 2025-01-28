package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderItem;
import com.refinedmods.refinedstorage.common.support.containermenu.ValidatedSlot;

import net.minecraft.world.Container;
import net.minecraft.world.level.Level;

public class PatternSlot extends ValidatedSlot {
    public PatternSlot(final Container container, final int index, final int x, final int y, final Level level) {
        super(container, index, x, y, stack -> PatternProviderItem.isValid(stack, level));
    }
}
