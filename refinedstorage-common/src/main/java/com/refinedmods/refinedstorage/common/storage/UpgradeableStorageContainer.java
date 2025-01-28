package com.refinedmods.refinedstorage.common.storage;

import net.minecraft.world.item.ItemStack;

public interface UpgradeableStorageContainer {
    StorageVariant getVariant();

    void transferTo(ItemStack from, ItemStack to);
}
