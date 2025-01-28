package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.autocrafting.PatternItem;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;

import net.minecraft.world.item.ItemStack;

class PatternOutputContainer extends FilteredContainer {
    PatternOutputContainer() {
        super(1, PatternGridBlockEntity::isValidPattern);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return stack.getItem() instanceof PatternItem patternItem && patternItem.hasMapping(stack);
    }
}
