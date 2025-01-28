package com.refinedmods.refinedstorage.api.storage.root;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.listenable.ListenableResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class RootStorageImpl implements RootStorage {
    protected final CompositeStorageImpl storage;
    private final ListenableResourceList list;
    private final Set<RootStorageListener> listeners;

    public RootStorageImpl() {
        this(MutableResourceListImpl.create(), new HashSet<>());
    }

    public RootStorageImpl(final MutableResourceList list) {
        this(list, new HashSet<>());
    }

    public RootStorageImpl(final MutableResourceList list, final Set<RootStorageListener> listeners) {
        this.list = new ListenableResourceList(list);
        this.storage = new CompositeStorageImpl(this.list);
        this.listeners = listeners;
    }

    @Override
    public void sortSources() {
        storage.sortSources();
    }

    @Override
    public void addSource(final Storage source) {
        storage.addSource(source);
    }

    @Override
    public void removeSource(final Storage source) {
        storage.removeSource(source);
    }

    @Override
    public boolean hasSource(final Predicate<Storage> matcher) {
        return storage.getSources().stream().anyMatch(matcher);
    }

    @Override
    public void addListener(final RootStorageListener listener) {
        list.addListener(listener);
        listeners.add(listener);
    }

    @Override
    public void removeListener(final RootStorageListener listener) {
        list.removeListener(listener);
        listeners.remove(listener);
    }

    @Override
    public long get(final ResourceKey resource) {
        return list.get(resource);
    }

    @Override
    public boolean contains(final ResourceKey resource) {
        return list.contains(resource);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return storage.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long intercepted = action == Action.EXECUTE ? interceptInsert(resource, amount, actor) : 0;
        if (intercepted == amount) {
            return amount;
        }
        final long inserted = storage.insert(resource, amount - intercepted, action, actor);
        return inserted + intercepted;
    }

    private long interceptInsert(final ResourceKey resource, final long amount, final Actor actor) {
        long totalReserved = 0;
        long totalIntercepted = 0;
        for (final RootStorageListener listener : listeners) {
            final long amountRemaining = amount - totalReserved;
            final RootStorageListener.InterceptResult result = listener.beforeInsert(resource, amountRemaining, actor);
            if (result.reserved() > amountRemaining) {
                throw new IllegalStateException(
                    "Listener %s indicated it reserved %d while the original available amount was %d"
                        .formatted(listener, result.reserved(), amountRemaining));
            }
            totalReserved += result.reserved();
            totalIntercepted += result.intercepted();
            if (totalReserved == amount) {
                return totalIntercepted;
            }
        }
        return totalIntercepted;
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return storage.getAll();
    }

    @Override
    public long getStored() {
        return storage.getStored();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return storage.findTrackedResourceByActorType(resource, actorType);
    }
}
