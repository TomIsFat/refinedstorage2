package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.api.core.NullableType;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;

class StonecutterInputContainer extends SimpleContainer implements ContainerListener {
    private List<RecipeHolder<StonecutterRecipe>> recipes = Collections.emptyList();
    private final Supplier<Level> levelSupplier;
    private int selectedRecipe;

    StonecutterInputContainer(final Supplier<@NullableType Level> levelSupplier) {
        super(1);
        this.levelSupplier = levelSupplier;
        addListener(this);
    }

    List<RecipeHolder<StonecutterRecipe>> getRecipes() {
        return recipes;
    }

    int getSelectedRecipe() {
        return selectedRecipe;
    }

    boolean hasSelectedRecipe() {
        return selectedRecipe >= 0;
    }

    void setSelectedRecipe(final int idx) {
        this.selectedRecipe = idx;
    }

    @Override
    public void containerChanged(final Container container) {
        final Level level = levelSupplier.get();
        if (level == null) {
            return;
        }
        this.selectedRecipe = -1;
        this.updateRecipes(level);
    }

    void updateRecipes(final Level level) {
        final ItemStack input = getItem(0);
        if (input.isEmpty()) {
            recipes = Collections.emptyList();
            return;
        }
        recipes = level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, new SingleRecipeInput(input), level);
    }
}
