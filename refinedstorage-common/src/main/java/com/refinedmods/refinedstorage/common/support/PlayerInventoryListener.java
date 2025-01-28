package com.refinedmods.refinedstorage.common.support;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface PlayerInventoryListener {
    void changed(ItemStack before, ItemStack after);
}
