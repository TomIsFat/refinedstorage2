package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;

import net.minecraft.world.item.ItemStack;

public record InWorldExternalPatternSinkKey(String name, ItemStack stack) implements ExternalPatternSinkKey {
}
