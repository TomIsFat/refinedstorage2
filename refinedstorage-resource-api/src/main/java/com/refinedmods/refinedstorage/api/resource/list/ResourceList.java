package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * Represents a list of a resource of an arbitrary type.
 * A basic implementation of this class can be found in {@link ResourceListImpl}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface ResourceList {
    /**
     * Adds a given resource to the list.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @return the result of the operation
     */
    OperationResult add(ResourceKey resource, long amount);

    /**
     * Adds a given resource to the list.
     * Shorthand for {@link #add(ResourceKey, long)}.
     *
     * @param resourceAmount the resource and the amount
     * @return the result of the operation
     */
    default OperationResult add(ResourceAmount resourceAmount) {
        return add(resourceAmount.resource(), resourceAmount.amount());
    }

    /**
     * Removes an amount of a certain resource in the list.
     * If the amount reaches 0 due to this removal, the resource is removed from the list.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @return a result if the removal operation was successful, otherwise an empty {@link Optional}
     */
    Optional<OperationResult> remove(ResourceKey resource, long amount);

    /**
     * Removes an amount of a certain resource in the list.
     * If the amount reaches 0 due to this removal, the resource is removed from the list.
     * Shorthand for {@link #remove(ResourceKey, long)}.
     *
     * @param resourceAmount the resource and the amount
     * @return a result if the removal operation was successful, otherwise an empty {@link Optional}
     */
    default Optional<OperationResult> remove(ResourceAmount resourceAmount) {
        return remove(resourceAmount.resource(), resourceAmount.amount());
    }

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
     * Clears the list.
     */
    void clear();

    /**
     * Copies the list.
     */
    ResourceList copy();

    /**
     * Represents the result of an operation in a {@link ResourceList}.
     *
     * @param resource  the resource affected by the operation
     * @param amount    teh current amount in the list
     * @param change    the delta caused by the operation
     * @param available whether this resource is still available in the list, or if it was removed
     */
    @API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
    record OperationResult(ResourceKey resource, long amount, long change, boolean available) {
    }
}
