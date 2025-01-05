package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public class PatternBuilder {
    private final PatternType type;
    private final UUID id;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<ResourceAmount> outputs = new ArrayList<>();

    private PatternBuilder(final UUID id, final PatternType type) {
        this.id = id;
        this.type = type;
    }

    public static PatternBuilder pattern() {
        return pattern(PatternType.INTERNAL);
    }

    public static PatternBuilder pattern(final PatternType type) {
        return pattern(UUID.randomUUID(), type);
    }

    public static PatternBuilder pattern(final UUID id, final PatternType type) {
        return new PatternBuilder(id, type);
    }

    public IngredientBuilder ingredient(final long amount) {
        return new IngredientBuilder(amount);
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
        return new Pattern(id, ingredients, outputs, type);
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
