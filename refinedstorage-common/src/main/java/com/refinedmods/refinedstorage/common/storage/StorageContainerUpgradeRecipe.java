package com.refinedmods.refinedstorage.common.storage;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class StorageContainerUpgradeRecipe<T extends StorageVariant> extends ShapelessRecipe {
    private final Set<Item> validSourceContainers;
    private final T[] variants;
    private final T to;

    public StorageContainerUpgradeRecipe(final T[] variants,
                                         final T to,
                                         final Function<T, ItemLike> containerProvider) {
        super(
            "",
            CraftingBookCategory.MISC,
            containerProvider.apply(to).asItem().getDefaultInstance(),
            getIngredients(variants, to, containerProvider)
        );
        this.validSourceContainers = getValidSourceContainers(to, variants, containerProvider);
        this.variants = variants;
        this.to = to;
    }

    private static <T extends StorageVariant> NonNullList<Ingredient> getIngredients(
        final T[] variants,
        final T to,
        final Function<T, ItemLike> containerProvider
    ) {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(getValidSourceContainers(to, variants, containerProvider).toArray(new Item[0])));
        ingredients.add(Ingredient.of(to.getStoragePart()));
        return ingredients;
    }

    public T[] getVariants() {
        return variants;
    }

    public T getTo() {
        return to;
    }

    private static <T extends StorageVariant> Set<Item> getValidSourceContainers(
        final T destination,
        final T[] variants,
        final Function<T, ItemLike> containerProvider
    ) {
        if (destination.getCapacity() == null) {
            return Set.of();
        }
        return Arrays.stream(variants)
            .filter(variant -> variant.getCapacity() != null && variant.getCapacity() < destination.getCapacity())
            .map(containerProvider)
            .map(ItemLike::asItem)
            .collect(Collectors.toSet());
    }

    @Override
    public ItemStack assemble(final CraftingInput input, final HolderLookup.Provider provider) {
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack fromDisk = input.getItem(i);
            if (fromDisk.getItem() instanceof UpgradeableStorageContainer from
                && validSourceContainers.contains(fromDisk.getItem())) {
                final ItemStack toDisk = getResultItem(provider).copy();
                from.transferTo(fromDisk, toDisk);
                return toDisk;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(final CraftingInput input, final Level level) {
        int diskCount = 0;
        int partCount = 0;
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack inputStack = input.getItem(i);
            if (validSourceContainers.contains(inputStack.getItem())) {
                diskCount++;
            } else if (inputStack.getItem() == to.getStoragePart()) {
                partCount++;
            }
            if (diskCount == 1 && partCount == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput input) {
        final NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof UpgradeableStorageContainer from
                && validSourceContainers.contains(stack.getItem())) {
                final Item storagePart = from.getVariant().getStoragePart();
                if (storagePart != null) {
                    remainingItems.set(i, storagePart.getDefaultInstance());
                }
            }
        }
        return remainingItems;
    }
}
