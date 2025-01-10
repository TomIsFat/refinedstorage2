package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * Represents a list of resources of an arbitrary type.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface ResourceList {
    /**
     * Retrieves all resources and their amounts from the list.
     *
     * @return a list of resource amounts
     */
    Collection<ResourceAmount> copyState();

    /**
     * @return set of resources contained in this list
     */
    Set<ResourceKey> getAll();

    /**
     * @param resource the resource
     * @return the amount stored, or zero if not stored
     */
    long get(ResourceKey resource);

    /**
     * @param resource the resource
     * @return whether the list contains this resource
     */
    boolean contains(ResourceKey resource);

    /**
     * @return whether the list is empty
     */
    boolean isEmpty();
}
