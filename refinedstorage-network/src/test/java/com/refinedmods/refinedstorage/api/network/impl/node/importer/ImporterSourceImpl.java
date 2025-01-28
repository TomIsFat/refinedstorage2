package com.refinedmods.refinedstorage.api.network.impl.node.importer;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class ImporterSourceImpl implements ImporterSource {
    private final List<ResourceKey> resources;
    private final StorageImpl storage = new StorageImpl();

    ImporterSourceImpl(final ResourceKey... resources) {
        this.resources = Arrays.stream(resources).toList();
    }

    ImporterSourceImpl add(final ResourceKey resource, final long amount) {
        storage.insert(resource, amount, Action.EXECUTE, Actor.EMPTY);
        return this;
    }

    @Override
    public Iterator<ResourceKey> getResources() {
        return resources.iterator();
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        // Extract a maximum of 5 to ensure that we try to extract multiple times from different slots.
        return storage.extract(resource, Math.min(amount, 5), action, actor);
    }

    Collection<ResourceAmount> getAll() {
        return storage.getAll();
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return storage.insert(resource, amount, action, actor);
    }
}
