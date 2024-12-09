package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

class StonecutterPattern implements Pattern {
    private final UUID id;
    private final ItemResource input;
    private final ItemResource output;
    private final List<Ingredient> ingredients;
    private final List<ResourceAmount> outputs;
    private final Set<ResourceKey> inputResources;
    private final Set<ResourceKey> outputResources;

    StonecutterPattern(final UUID id, final ItemResource input, final ItemResource output) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.inputResources = Set.of(input);
        this.outputResources = Set.of(output);
        this.outputs = List.of(new ResourceAmount(output, 1));
        this.ingredients = List.of(new Ingredient(1, List.of(input)));
    }

    @Override
    public Set<ResourceKey> getOutputResources() {
        return outputResources;
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public List<ResourceAmount> getOutputs() {
        return outputs;
    }

    @Override
    public Set<ResourceKey> getInputResources() {
        return inputResources;
    }

    ItemResource getInput() {
        return input;
    }

    ItemResource getOutput() {
        return output;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final StonecutterPattern that = (StonecutterPattern) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
