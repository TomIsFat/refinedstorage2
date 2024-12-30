package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.6")
public record Pattern(List<Ingredient> ingredients, List<ResourceAmount> outputs) {
    public Pattern(final List<Ingredient> ingredients, final List<ResourceAmount> outputs) {
        CoreValidations.validateNotEmpty(ingredients, "Ingredients cannot be empty");
        CoreValidations.validateNotEmpty(outputs, "Outputs cannot be empty");
        this.ingredients = List.copyOf(ingredients);
        this.outputs = List.copyOf(outputs);
    }
}
