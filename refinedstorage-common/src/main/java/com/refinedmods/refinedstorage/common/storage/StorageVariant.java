package com.refinedmods.refinedstorage.common.storage;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;

public interface StorageVariant {
    @Nullable
    Long getCapacity();

    @Nullable
    Item getStoragePart();
}
