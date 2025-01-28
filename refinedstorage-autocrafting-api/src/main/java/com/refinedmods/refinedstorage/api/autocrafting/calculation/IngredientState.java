package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Optional;

class IngredientState {
    private final long amount;
    private final List<ResourceKey> possibilities;
    private int pos;

    IngredientState(final Ingredient ingredient, final CraftingState state) {
        this.amount = ingredient.amount();
        this.possibilities = ingredient.inputs()
            .stream()
            .sorted(state.storageSorter())
            .sorted(state.internalStorageSorter())
            .toList();
    }

    ResourceKey get() {
        return possibilities.get(pos);
    }

    long amount() {
        return amount;
    }

    Optional<ResourceKey> cycle() {
        if (pos + 1 >= possibilities.size()) {
            return Optional.empty();
        }
        pos++;
        return Optional.of(get());
    }
}
