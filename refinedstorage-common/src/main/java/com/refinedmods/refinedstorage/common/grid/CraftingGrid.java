package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

public interface CraftingGrid extends Grid {
    RecipeMatrixContainer getCraftingMatrix();

    ResultContainer getCraftingResult();

    NonNullList<ItemStack> getRemainingItems(Player player, CraftingInput input);

    ExtractTransaction startExtractTransaction(Player player, boolean directCommit);

    boolean clearMatrix(Player player, boolean toPlayerInventory);

    void transferRecipe(Player player, List<List<ItemResource>> recipe);

    void acceptQuickCraft(Player player, ItemStack craftedStack);
}
