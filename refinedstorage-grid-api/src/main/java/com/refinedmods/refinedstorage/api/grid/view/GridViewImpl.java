package com.refinedmods.refinedstorage.api.grid.view;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridViewImpl implements GridView {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridViewImpl.class);

    private final MutableResourceList backingList;
    private final Comparator<GridResource> identitySort;
    private final GridResourceFactory resourceFactory;
    private final Map<ResourceKey, TrackedResource> trackedResources = new HashMap<>();
    private final Set<ResourceKey> autocraftableResources;

    private ViewList viewList = new ViewList(new ArrayList<>(), new HashMap<>());
    private GridSortingType sortingType;
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private BiPredicate<GridView, GridResource> filter = (view, resource) -> true;
    @Nullable
    private Runnable listener;
    private boolean preventSorting;

    /**
     * @param resourceFactory         a factory that transforms a resource amount to a grid resource
     * @param backingList             the backing list
     * @param initialTrackedResources initial tracked resources state
     * @param identitySortingType     a sorting type required to keep a consistent sorting order with quantity sorting
     * @param defaultSortingType      the default sorting type
     * @param autocraftableResources  resources which are autocraftable and must stay in the view list
     */
    public GridViewImpl(final GridResourceFactory resourceFactory,
                        final MutableResourceList backingList,
                        final Map<ResourceKey, TrackedResource> initialTrackedResources,
                        final Set<ResourceKey> autocraftableResources,
                        final GridSortingType identitySortingType,
                        final GridSortingType defaultSortingType) {
        this.resourceFactory = resourceFactory;
        this.identitySort = identitySortingType.apply(this);
        this.sortingType = defaultSortingType;
        this.backingList = backingList;
        this.trackedResources.putAll(initialTrackedResources);
        this.autocraftableResources = autocraftableResources;
    }

    @Override
    public void setListener(@Nullable final Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void setSortingType(final GridSortingType sortingType) {
        this.sortingType = sortingType;
    }

    @Override
    public BiPredicate<GridView, GridResource> setFilterAndSort(final BiPredicate<GridView, GridResource> predicate) {
        final BiPredicate<GridView, GridResource> previousPredicate = filter;
        this.filter = predicate;
        sort();
        return previousPredicate;
    }

    @Override
    public boolean setPreventSorting(final boolean changedPreventSorting) {
        final boolean changed = preventSorting != changedPreventSorting;
        this.preventSorting = changedPreventSorting;
        return changed;
    }

    @Override
    public void setSortingDirection(final GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    @Override
    public Optional<TrackedResource> getTrackedResource(final ResourceKey resource) {
        return Optional.ofNullable(trackedResources.get(resource));
    }

    @Override
    public long getAmount(final ResourceKey resource) {
        return backingList.get(resource);
    }

    @Override
    public boolean isAutocraftable(final ResourceKey resource) {
        return autocraftableResources.contains(resource);
    }

    @Override
    public void sort() {
        LOGGER.debug("Sorting grid view");
        viewList = createViewList();
        notifyListener();
    }

    private ViewList createViewList() {
        final List<GridResource> list = new ArrayList<>();
        final Map<ResourceKey, GridResource> index = new HashMap<>();
        for (final ResourceKey resource : backingList.getAll()) {
            tryAddResourceIntoViewList(resource, list, index, autocraftableResources.contains(resource));
        }
        for (final ResourceKey autocraftableResource : autocraftableResources) {
            if (!index.containsKey(autocraftableResource)) {
                tryAddResourceIntoViewList(autocraftableResource, list, index, true);
            }
        }
        list.sort(getComparator());
        return new ViewList(list, index);
    }

    private void tryAddResourceIntoViewList(final ResourceKey resource,
                                            final List<GridResource> list,
                                            final Map<ResourceKey, GridResource> index,
                                            final boolean autocraftable) {
        final GridResource existingGridResource = viewList.index.get(resource);
        if (existingGridResource != null) {
            tryAddGridResourceIntoViewList(existingGridResource, list, index, resource);
        } else {
            resourceFactory.apply(resource, autocraftable).ifPresent(
                gridResource -> tryAddGridResourceIntoViewList(gridResource, list, index, resource)
            );
        }
    }

    private void tryAddGridResourceIntoViewList(final GridResource gridResource,
                                                final List<GridResource> list,
                                                final Map<ResourceKey, GridResource> index,
                                                final ResourceKey resource) {
        if (filter.test(this, gridResource)) {
            list.add(gridResource);
            index.put(resource, gridResource);
        }
    }

    @Override
    public void onChange(final ResourceKey resource,
                         final long amount,
                         @Nullable final TrackedResource trackedResource) {
        final boolean wasAvailable = backingList.contains(resource);
        final MutableResourceList.OperationResult operationResult = updateBackingList(resource, amount);
        updateOrRemoveTrackedResource(resource, trackedResource);
        final GridResource gridResource = viewList.index.get(resource);
        if (gridResource != null) {
            LOGGER.debug("{} was already found in the view list", resource);
            if (!wasAvailable) {
                reinsertIntoViewList(resource, gridResource);
            } else {
                handleChangeForExistingResource(resource, operationResult, gridResource);
            }
        } else {
            LOGGER.debug("{} is a new resource, adding it into the view list if filter allows it", resource);
            handleChangeForNewResource(resource);
        }
    }

    private MutableResourceList.OperationResult updateBackingList(final ResourceKey resource, final long amount) {
        if (amount < 0) {
            return backingList.remove(resource, Math.abs(amount)).orElseThrow(RuntimeException::new);
        } else {
            return backingList.add(resource, amount);
        }
    }

    private void updateOrRemoveTrackedResource(final ResourceKey resource,
                                               @Nullable final TrackedResource trackedResource) {
        if (trackedResource == null) {
            trackedResources.remove(resource);
        } else {
            trackedResources.put(resource, trackedResource);
        }
    }

    private void reinsertIntoViewList(final ResourceKey resource, final GridResource oldGridResource) {
        LOGGER.debug("{} was removed from backing list, reinserting now into the view list", resource);
        final GridResource newResource = resourceFactory.apply(
            resource,
            autocraftableResources.contains(resource)
        ).orElseThrow();
        viewList.index.put(resource, newResource);
        final int index = CoreValidations.validateNotNegative(
            viewList.list.indexOf(oldGridResource),
            "Failed to reinsert resource into view list, even though it was still present in the view index"
        );
        viewList.list.set(index, newResource);
    }

    private void handleChangeForExistingResource(final ResourceKey resource,
                                                 final MutableResourceList.OperationResult operationResult,
                                                 final GridResource gridResource) {
        final boolean noLongerAvailable = !operationResult.available();
        final boolean canBeSorted = !preventSorting;
        if (canBeSorted) {
            LOGGER.debug("Actually updating {} resource in the view list", resource);
            updateExistingResourceInViewList(resource, gridResource, noLongerAvailable);
        } else if (noLongerAvailable) {
            LOGGER.debug("{} is no longer available", resource);
        } else {
            LOGGER.debug("{} can't be sorted, preventing sorting is on", resource);
        }
    }

    private void updateExistingResourceInViewList(final ResourceKey resource,
                                                  final GridResource gridResource,
                                                  final boolean noLongerAvailable) {
        viewList.list.remove(gridResource);
        if (noLongerAvailable && !autocraftableResources.contains(resource)) {
            viewList.index.remove(resource);
            notifyListener();
        } else {
            addIntoView(gridResource);
            notifyListener();
        }
    }

    private void handleChangeForNewResource(final ResourceKey resource) {
        final GridResource gridResource = resourceFactory.apply(resource, false)
            .orElseThrow();
        if (filter.test(this, gridResource)) {
            LOGGER.debug("Filter allowed, actually adding {}", resource);
            viewList.index.put(resource, gridResource);
            addIntoView(gridResource);
            notifyListener();
        }
    }

    private void addIntoView(final GridResource resource) {
        // Calculate the position according to sorting rules.
        final int wouldBePosition = Collections.binarySearch(viewList.list, resource, getComparator());
        // Most of the time, the "would be" position is negative, indicating that the resource wasn't found yet in the
        // list, comparing with sorting rules. The absolute of this position would be the "real" position if sorted.
        if (wouldBePosition < 0) {
            viewList.list.add(-wouldBePosition - 1, resource);
        } else {
            // If the "would-be" position is positive, this means that the resource is already contained in the list,
            // comparing with sorting rules.
            // This doesn't mean that the *exact* resource is already in the list, but that is purely "contained"
            // in the list when comparing with sorting rules.
            // For example: a resource with different identity but the same name (in Minecraft: an enchanted book
            // with different NBT).
            // In that case, just insert it after the "existing" resource.
            viewList.list.add(wouldBePosition + 1, resource);
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }

    private Comparator<GridResource> getComparator() {
        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid resources have the same quantity, their order would otherwise not be preserved.
        final Comparator<GridResource> comparator = sortingType.apply(this).thenComparing(identitySort);
        if (sortingDirection == GridSortingDirection.ASCENDING) {
            return comparator;
        }
        return comparator.reversed();
    }

    @Override
    public List<GridResource> getViewList() {
        return viewList.list;
    }

    @Override
    public MutableResourceList copyBackingList() {
        return backingList.copy();
    }

    @Override
    public void clear() {
        backingList.clear();
        viewList.index.clear();
        trackedResources.clear();
        viewList.list.clear();
    }

    private record ViewList(List<GridResource> list, Map<ResourceKey, GridResource> index) {
    }
}
