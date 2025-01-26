package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternInputSinkKey;

import net.minecraft.world.item.ItemStack;

public record InWorldExternalPatternInputSinkKey(String name, ItemStack stack) implements ExternalPatternInputSinkKey {
}
