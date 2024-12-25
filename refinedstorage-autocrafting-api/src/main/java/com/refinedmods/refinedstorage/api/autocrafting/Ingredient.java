package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.List;

public class Ingredient {
    private final long amount;
    private final List<ResourceKey> inputs;

    public Ingredient(final long amount, final List<? extends ResourceKey> inputs) {
        this.amount = amount;
        this.inputs = Collections.unmodifiableList(inputs);
    }

    public boolean isEmpty() {
        return inputs.isEmpty();
    }

    public int size() {
        return inputs.size();
    }

    public long getAmount() {
        return amount;
    }

    public ResourceKey get(final int index) {
        return inputs.get(index);
    }

    @Override
    public String toString() {
        return "Ingredient{"
            + "amount=" + amount
            + ", inputs=" + inputs
            + '}';
    }
}
