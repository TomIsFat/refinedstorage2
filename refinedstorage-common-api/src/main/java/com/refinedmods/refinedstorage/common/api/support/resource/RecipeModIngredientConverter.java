package com.refinedmods.refinedstorage.common.api.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
public interface RecipeModIngredientConverter {
    Optional<PlatformResourceKey> convertToResource(Object ingredient);

    Optional<ResourceAmount> convertToResourceAmount(Object ingredient);

    Optional<Object> convertToIngredient(PlatformResourceKey resource);
}
