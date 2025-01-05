package com.refinedmods.refinedstorage.api.storage.root;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage.api.storage.Actor;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface RootStorageListener extends ResourceListListener {
    /**
     * Called before a resource is actually inserted into the storage.
     * This is useful for modifying the amount before it is inserted.
     *
     * @param resource the resource
     * @param amount   the amount about to be inserted
     * @param actor    the actor
     * @return the amount intercepted
     */
    long beforeInsert(ResourceKey resource, long amount, Actor actor);
}
