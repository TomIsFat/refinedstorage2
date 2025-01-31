package com.refinedmods.refinedstorage.api.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GridResourceImpl implements GridResource {
    private final ResourceKey resource;
    private final Map<GridResourceAttributeKey, Set<String>> attributes;
    private boolean zeroed;

    public GridResourceImpl(final ResourceKey resource) {
        this.resource = resource;
        this.attributes = Map.of(
            FakeGridResourceAttributeKeys.MOD_ID, Set.of(resource.toString()),
            FakeGridResourceAttributeKeys.MOD_NAME, Set.of(resource.toString())
        );
    }

    public GridResourceImpl zeroed() {
        setZeroed(true);
        return this;
    }

    @Override
    public Optional<TrackedResource> getTrackedResource(final GridView view) {
        return view.getTrackedResource(resource);
    }

    @Override
    public long getAmount(final GridView view) {
        return view.getAmount(resource);
    }

    @Override
    public String getName() {
        return resource.toString();
    }

    @Override
    public Set<String> getAttribute(final GridResourceAttributeKey key) {
        return attributes.getOrDefault(key, Collections.emptySet());
    }

    @Override
    public boolean isZeroed() {
        return zeroed;
    }

    @Override
    public void setZeroed(final boolean zeroed) {
        this.zeroed = zeroed;
    }

    @Override
    public String toString() {
        return resource.toString();
    }
}
