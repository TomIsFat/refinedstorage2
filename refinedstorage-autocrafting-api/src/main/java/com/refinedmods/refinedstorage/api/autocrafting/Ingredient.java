package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public record Ingredient(long amount, List<ResourceKey> inputs) {
    public Ingredient(final long amount, final List<ResourceKey> inputs) {
        CoreValidations.validateLargerThanZero(amount, "Amount must be larger than zero");
        CoreValidations.validateNotEmpty(inputs, "Inputs cannot be empty");
        this.amount = amount;
        this.inputs = List.copyOf(inputs);
    }
}
