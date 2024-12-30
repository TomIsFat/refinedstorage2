package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderItem;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterScreen;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerScreen;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class PatternRendering {
    private PatternRendering() {
    }

    public static boolean canDisplayOutput(final ItemStack stack) {
        if (!(stack.getItem() instanceof PatternProviderItem)) {
            return false;
        }
        if (Screen.hasShiftDown()) {
            return true;
        }
        return canDisplayOutputInScreen(stack);
    }

    private static boolean canDisplayOutputInScreen(final ItemStack stack) {
        final Screen screen = Minecraft.getInstance().screen;
        return switch (screen) {
            case PatternGridScreen patternGridScreen -> patternGridScreen.getMenu().isPatternInOutput(stack);
            case AutocrafterScreen autocrafterScreen -> autocrafterScreen.getMenu().containsPattern(stack);
            case AutocrafterManagerScreen autocrafterManagerScreen ->
                autocrafterManagerScreen.getMenu().containsPattern(stack);
            case null, default -> false;
        };
    }

    public static Optional<ItemStack> getOutput(final ItemStack stack) {
        final Level level = ClientPlatformUtil.getClientLevel();
        if (level == null) {
            return Optional.empty();
        }
        if (stack.getItem() instanceof PatternProviderItem patternProviderItem) {
            return patternProviderItem.getOutput(stack, level);
        }
        return Optional.empty();
    }
}
