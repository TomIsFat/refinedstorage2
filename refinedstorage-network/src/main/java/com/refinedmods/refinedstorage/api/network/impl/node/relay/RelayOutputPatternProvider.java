package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternListener;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class RelayOutputPatternProvider implements PatternProvider, PatternListener {
    private final Filter filter = new Filter();
    private final Set<ParentContainer> parents = new HashSet<>();
    @Nullable
    private AutocraftingNetworkComponent delegate;

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
        final AutocraftingNetworkComponent oldDelegate = delegate;
        setDelegate(null);
        action.run();
        setDelegate(oldDelegate);
    }

    void setDelegate(@Nullable final AutocraftingNetworkComponent delegate) {
        if (this.delegate != null) {
            parents.forEach(parent -> getPatterns().forEach(pattern -> parent.remove(this, pattern)));
            this.delegate.removeListener(this);
        }
        this.delegate = delegate;
        if (delegate != null) {
            parents.forEach(parent -> getPatterns().forEach(pattern -> parent.add(this, pattern, 0)));
            delegate.addListener(this);
        }
    }

    boolean hasDelegate() {
        return delegate != null;
    }

    private Set<Pattern> getPatterns() {
        if (delegate == null) {
            return Collections.emptySet();
        }
        return delegate.getPatterns().stream().filter(this::isPatternAllowed).collect(Collectors.toSet());
    }

    private boolean isPatternAllowed(final Pattern pattern) {
        return pattern.outputs().stream().map(ResourceAmount::resource).anyMatch(filter::isAllowed);
    }

    @Override
    public void onAdded(final Pattern pattern) {
        if (delegate == null || !isPatternAllowed(pattern) || delegate.contains(delegate)) {
            return;
        }
        parents.forEach(parent -> parent.add(this, pattern, 0));
    }

    @Override
    public void onRemoved(final Pattern pattern) {
        if (delegate == null || !isPatternAllowed(pattern) || delegate.contains(delegate)) {
            return;
        }
        parents.forEach(parent -> parent.remove(this, pattern));
    }

    @Override
    public boolean contains(final AutocraftingNetworkComponent component) {
        return component == delegate || (delegate != null && delegate.contains(component));
    }

    @Override
    public void addTask(final Task task) {
        // TODO(feat): relay support
    }

    @Override
    public void onAddedIntoContainer(final ParentContainer parentContainer) {
        if (delegate != null) {
            delegate.getPatterns().forEach(pattern -> parentContainer.add(this, pattern, 0));
        }
        parents.add(parentContainer);
    }

    @Override
    public void onRemovedFromContainer(final ParentContainer parentContainer) {
        if (delegate != null) {
            delegate.getPatterns().forEach(pattern -> parentContainer.remove(this, pattern));
        }
        parents.remove(parentContainer);
    }

    @Override
    public boolean accept(final Collection<ResourceAmount> resources, final Action action) {
        return false; // TODO(feat): relay support
    }
}
