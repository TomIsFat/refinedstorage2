package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apiguardian.api.API;

/**
 * An implementation of a {@link ResourceList} that stores the resource entries in a {@link HashMap}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class ResourceListImpl implements ResourceList {
    private final Map<ResourceKey, ResourceAmount> entries = new HashMap<>();

    @Override
    public ResourceListOperationResult add(final ResourceKey resource, final long amount) {
        final ResourceAmount existing = entries.get(resource);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(resource, amount);
        }
    }

    private ResourceListOperationResult addToExisting(final ResourceAmount resourceAmount, final long amount) {
        resourceAmount.increment(amount);

        return new ResourceListOperationResult(resourceAmount, amount, true);
    }

    private ResourceListOperationResult addNew(final ResourceKey resource, final long amount) {
        final ResourceAmount resourceAmount = new ResourceAmount(resource, amount);
        entries.put(resource, resourceAmount);
        return new ResourceListOperationResult(resourceAmount, amount, true);
    }

    @Override
    public Optional<ResourceListOperationResult> remove(final ResourceKey resource, final long amount) {
        ResourceAmount.validate(resource, amount);

        final ResourceAmount existing = entries.get(resource);
        if (existing != null) {
            if (existing.getAmount() - amount <= 0) {
                return removeCompletely(existing);
            } else {
                return removePartly(amount, existing);
            }
        }

        return Optional.empty();
    }

    private Optional<ResourceListOperationResult> removePartly(final long amount,
                                                               final ResourceAmount resourceAmount) {
        resourceAmount.decrement(amount);

        return Optional.of(new ResourceListOperationResult(resourceAmount, -amount, true));
    }

    private Optional<ResourceListOperationResult> removeCompletely(final ResourceAmount resourceAmount) {
        entries.remove(resourceAmount.getResource());

        return Optional.of(new ResourceListOperationResult(
            resourceAmount,
            -resourceAmount.getAmount(),
            false
        ));
    }

    @Override
    public Optional<ResourceAmount> get(final ResourceKey resource) {
        return Optional.ofNullable(entries.get(resource));
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return entries.values();
    }

    @Override
    public void clear() {
        entries.clear();
    }
}
