package com.refinedmods.refinedstorage.common.grid.view;

import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;

import java.util.Optional;

public class CompositeGridResourceFactory implements GridResourceFactory {
    private final PlatformRegistry<ResourceType> resourceTypeRegistry;

    public CompositeGridResourceFactory(final PlatformRegistry<ResourceType> resourceTypeRegistry) {
        this.resourceTypeRegistry = resourceTypeRegistry;
    }

    @Override
    public Optional<GridResource> apply(final ResourceKey resource, final boolean autocraftable) {
        return resourceTypeRegistry.getAll()
            .stream()
            .flatMap(type -> type.toGridResource(resource, autocraftable).stream())
            .findFirst();
    }
}
