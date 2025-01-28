package com.refinedmods.refinedstorage.api.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Transforms resources into {@link GridResource}s.
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface GridResourceFactory {
    /**
     * Transforms a {@link com.refinedmods.refinedstorage.api.resource.ResourceKey} into a {@link GridResource}.
     *
     * @param resource      the resource
     * @param autocraftable whether the resource is autocraftable
     * @return the grid resource, if applicable
     */
    Optional<GridResource> apply(ResourceKey resource, boolean autocraftable);
}
