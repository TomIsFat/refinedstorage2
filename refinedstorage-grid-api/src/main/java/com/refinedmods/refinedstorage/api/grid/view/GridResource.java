package com.refinedmods.refinedstorage.api.grid.view;

import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * Represents a resource in the grid.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridResource {
    Optional<TrackedResource> getTrackedResource(GridView view);

    long getAmount(GridView view);

    String getName();

    Set<String> getAttribute(GridResourceAttributeKey key);

    boolean isAutocraftable();
}
