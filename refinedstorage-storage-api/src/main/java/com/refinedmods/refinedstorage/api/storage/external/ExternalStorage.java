package com.refinedmods.refinedstorage.api.storage.external;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage.api.storage.composite.ParentComposite;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public class ExternalStorage implements CompositeAwareChild {
    private final ExternalStorageProvider provider;
    private final Set<ParentComposite> parents = new HashSet<>();
    private final MutableResourceList cache = MutableResourceListImpl.create();
    private final ExternalStorageListener listener;

    public ExternalStorage(final ExternalStorageProvider provider, final ExternalStorageListener listener) {
        this.provider = provider;
        this.listener = listener;
    }

    public ExternalStorageProvider getProvider() {
        return provider;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long extracted = provider.extract(resource, amount, action, actor);
        if (action == Action.EXECUTE && extracted > 0) {
            listener.beforeDetectChanges(resource, actor);
            detectChanges();
        }
        return extracted;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long inserted = provider.insert(resource, amount, action, actor);
        if (action == Action.EXECUTE && inserted > 0) {
            listener.beforeDetectChanges(resource, actor);
            detectChanges();
        }
        return inserted;
    }

    public boolean detectChanges() {
        final ResourceList updatedCache = buildCache();
        boolean hasChanges = detectCompleteRemovals(updatedCache);
        hasChanges |= detectAdditionsAndPartialRemovals(updatedCache);
        return hasChanges;
    }

    private boolean detectCompleteRemovals(final ResourceList updatedCache) {
        final Set<ResourceKey> removedInUpdatedCache = new HashSet<>();
        for (final ResourceKey inOldCache : cache.getAll()) {
            if (!updatedCache.contains(inOldCache)) {
                removedInUpdatedCache.add(inOldCache);
            }
        }
        removedInUpdatedCache.forEach(this::removeFromCache);
        return !removedInUpdatedCache.isEmpty();
    }

    private boolean detectAdditionsAndPartialRemovals(final ResourceList updatedCache) {
        boolean hasChanges = false;
        for (final ResourceKey resource : updatedCache.getAll()) {
            final long amountInUpdatedCache = updatedCache.get(resource);
            final long amountInOldCache = cache.get(resource);
            final boolean doesNotExistInOldCache = amountInOldCache == 0;
            if (doesNotExistInOldCache) {
                addToCache(resource, amountInUpdatedCache);
                hasChanges = true;
            } else {
                hasChanges |= detectPotentialDifference(resource, amountInUpdatedCache, amountInOldCache);
            }
        }
        return hasChanges;
    }

    private boolean detectPotentialDifference(
        final ResourceKey resource,
        final long amountInUpdatedCache,
        final long amountInOldCache
    ) {
        final long diff = amountInUpdatedCache - amountInOldCache;
        if (diff > 0) {
            addToCache(resource, diff);
            return true;
        } else if (diff < 0) {
            removeFromCache(resource, Math.abs(diff));
            return true;
        }
        return false;
    }

    private void addToCache(final ResourceKey resource, final long amount) {
        cache.add(resource, amount);
        parents.forEach(parent -> parent.addToCache(resource, amount));
    }

    private void removeFromCache(final ResourceKey resource) {
        removeFromCache(resource, cache.get(resource));
    }

    private void removeFromCache(final ResourceKey resource, final long amount) {
        cache.remove(resource, amount);
        parents.forEach(parent -> parent.removeFromCache(resource, amount));
    }

    private ResourceList buildCache() {
        final MutableResourceList list = MutableResourceListImpl.create();
        provider.iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return cache.copyState();
    }

    @Override
    public long getStored() {
        return getAll().stream().mapToLong(ResourceAmount::amount).sum();
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite parentComposite) {
        parents.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite parentComposite) {
        parents.remove(parentComposite);
    }

    @Override
    public Amount compositeInsert(final ResourceKey resource,
                                  final long amount,
                                  final Action action,
                                  final Actor actor) {
        final long inserted = insert(resource, amount, action, actor);
        return new Amount(inserted, 0);
    }

    @Override
    public Amount compositeExtract(final ResourceKey resource,
                                   final long amount,
                                   final Action action,
                                   final Actor actor) {
        final long extracted = extract(resource, amount, action, actor);
        return new Amount(extracted, 0);
    }
}
