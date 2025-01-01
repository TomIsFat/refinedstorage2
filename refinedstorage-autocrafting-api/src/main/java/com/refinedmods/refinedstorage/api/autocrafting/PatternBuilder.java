package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayList;
import java.util.List;

public class PatternBuilder {
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<ResourceAmount> outputs = new ArrayList<>();

    private PatternBuilder() {
    }

    public static PatternBuilder pattern() {
        return new PatternBuilder();
    }

    public IngredientBuilder ingredient(final long amount) {
        return new IngredientBuilder(amount);
    }

    public PatternBuilder ingredient(final Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public PatternBuilder ingredient(final ResourceKey input, final long amount) {
        ingredients.add(new Ingredient(amount, List.of(input)));
        return this;
    }

    public PatternBuilder output(final ResourceKey output, final long amount) {
        outputs.add(new ResourceAmount(output, amount));
        return this;
    }

    public Pattern build() {
        return new Pattern(ingredients, outputs);
    }

    public class IngredientBuilder {
        private final long amount;
        private final List<ResourceKey> inputs = new ArrayList<>();

        private IngredientBuilder(final long amount) {
            this.amount = amount;
        }

        public IngredientBuilder input(final ResourceKey input) {
            inputs.add(input);
            return this;
        }

        public PatternBuilder end() {
            ingredients.add(new Ingredient(amount, inputs));
            return PatternBuilder.this;
        }
    }
}
