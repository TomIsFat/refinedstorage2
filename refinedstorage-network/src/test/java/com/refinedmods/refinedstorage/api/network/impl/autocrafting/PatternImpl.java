package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PatternImpl implements Pattern {
    private final List<Ingredient> ingredients;
    private final List<ResourceAmount> outputs;

    public PatternImpl(final ResourceKey... outputs) {
        this(List.of(),
            Arrays.stream(outputs).map(output -> new ResourceAmount(output, 1)).toArray(ResourceAmount[]::new));
    }

    public PatternImpl(final List<Ingredient> ingredients, final ResourceAmount... outputs) {
        this.ingredients = ingredients;
        this.outputs = Arrays.asList(outputs);
    }

    @Override
    public Set<ResourceKey> getInputResources() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ResourceKey> getOutputResources() {
        return outputs.stream().map(ResourceAmount::resource).collect(Collectors.toSet());
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public List<ResourceAmount> getOutputs() {
        return outputs;
    }
}
