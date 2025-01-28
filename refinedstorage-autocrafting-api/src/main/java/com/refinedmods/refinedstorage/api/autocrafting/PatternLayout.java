package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;

import org.apiguardian.api.API;

/**
 * Represents a pattern layout. Multiple {@link Pattern}s can share the same layout.
 *
 * @param ingredients the ingredients
 * @param outputs     the outputs
 * @param type        the type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public record PatternLayout(List<Ingredient> ingredients, List<ResourceAmount> outputs, PatternType type) {
    public PatternLayout(final List<Ingredient> ingredients,
                         final List<ResourceAmount> outputs,
                         final PatternType type) {
        CoreValidations.validateNotEmpty(ingredients, "Ingredients cannot be empty");
        CoreValidations.validateNotEmpty(outputs, "Outputs cannot be empty");
        CoreValidations.validateNotNull(type, "Type cannot be null");
        this.ingredients = List.copyOf(ingredients);
        this.outputs = List.copyOf(outputs);
        this.type = type;
    }
}
