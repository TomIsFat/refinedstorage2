package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage.api.storage.composite.PriorityProvider;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageListener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

class RelayOutputStorage implements CompositeAwareChild, RootStorageListener, PriorityProvider {
    private final Set<ParentComposite> parentComposites = new HashSet<>();
    private final Filter filter = new Filter();

    @Nullable
    private StorageNetworkComponent delegate;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int insertPriority;
    private int extractPriority;

    boolean hasDelegate() {
        return delegate != null;
    }

    void setAccessMode(final AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    void setInsertPriority(final int insertPriority) {
        this.insertPriority = insertPriority;
    }

    void setExtractPriority(final int extractPriority) {
        this.extractPriority = extractPriority;
    }

    void setFilters(final Set<ResourceKey> filters) {
        reset(() -> filter.setFilters(filters));
    }

    void setFilterMode(final FilterMode filterMode) {
        reset(() -> filter.setMode(filterMode));
    }

    void setFilterNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        reset(() -> filter.setNormalizer(normalizer));
    }

    private void reset(final Runnable action) {
        final StorageNetworkComponent oldDelegate = delegate;
        setDelegate(null);
        action.run();
        setDelegate(oldDelegate);
    }

    void setDelegate(@Nullable final StorageNetworkComponent delegate) {
        if (this.delegate != null) {
            parentComposites.forEach(parent -> getAll().forEach(resourceAmount -> parent.removeFromCache(
                resourceAmount.resource(),
                resourceAmount.amount()
            )));
            this.delegate.removeListener(this);
        }
        this.delegate = delegate;
        if (delegate != null) {
            parentComposites.forEach(parent -> getAll().forEach(resourceAmount -> parent.addToCache(
                resourceAmount.resource(),
                resourceAmount.amount()
            )));
            delegate.addListener(this);
        }
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite parentComposite) {
        parentComposites.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite parentComposite) {
        parentComposites.remove(parentComposite);
    }

    @Override
    public boolean contains(final Storage storage) {
        return storage == delegate || (delegate != null && delegate.contains(storage));
    }

    @Override
    public Amount compositeInsert(final ResourceKey resource,
                                  final long amount,
                                  final Action action,
                                  final Actor actor) {
        if (delegate == null
            || accessMode == AccessMode.EXTRACT
            || delegate.contains(delegate)
            || !filter.isAllowed(resource)) {
            return Amount.ZERO;
        }
        final long inserted = delegate.insert(resource, amount, action, actor);
        return new Amount(inserted, 0);
    }

    @Override
    public Amount compositeExtract(final ResourceKey resource,
                                   final long amount,
                                   final Action action,
                                   final Actor actor) {
        if (delegate == null
            || accessMode == AccessMode.INSERT
            || delegate.contains(delegate)
            || !filter.isAllowed(resource)) {
            return Amount.ZERO;
        }
        final long extracted = delegate.extract(resource, amount, action, actor);
        return new Amount(extracted, 0);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        if (delegate == null || delegate.contains(delegate)) {
            return Collections.emptyList();
        }
        return delegate.getAll()
            .stream()
            .filter(resourceAmount -> filter.isAllowed(resourceAmount.resource()))
            .toList();
    }

    @Override
    public long getStored() {
        if (delegate == null || delegate.contains(delegate)) {
            return 0;
        }
        return delegate.getAll()
            .stream()
            .filter(resourceAmount -> filter.isAllowed(resourceAmount.resource()))
            .mapToLong(ResourceAmount::amount)
            .sum();
    }

    @Override
    public void changed(final MutableResourceList.OperationResult change) {
        if (delegate != null && delegate.contains(delegate)) {
            return;
        }
        final ResourceKey resource = change.resource();
        if (!filter.isAllowed(resource)) {
            return;
        }
        if (change.change() > 0) {
            parentComposites.forEach(parent -> parent.addToCache(resource, change.change()));
        } else {
            parentComposites.forEach(parent -> parent.removeFromCache(resource, -change.change()));
        }
    }

    @Override
    public int getInsertPriority() {
        return insertPriority;
    }

    @Override
    public int getExtractPriority() {
        return extractPriority;
    }

    @Override
    public InterceptResult beforeInsert(final ResourceKey resource, final long amount, final Actor actor) {
        return InterceptResult.EMPTY;
    }
}
