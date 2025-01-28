package com.refinedmods.refinedstorage.api.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.grid.TestResource.A;
import static com.refinedmods.refinedstorage.api.grid.TestResource.B;
import static com.refinedmods.refinedstorage.api.grid.TestResource.C;
import static com.refinedmods.refinedstorage.api.grid.TestResource.D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class GridViewImplTest {
    private GridViewBuilder viewBuilder;

    @BeforeEach
    void setUp() {
        viewBuilder = getViewBuilder((resource, craftable) -> Optional.of(new GridResourceImpl(resource, craftable)));
    }

    private static GridViewBuilderImpl getViewBuilder(final GridResourceFactory resourceFactory) {
        return new GridViewBuilderImpl(
            resourceFactory,
            view -> Comparator.comparing(GridResource::getName),
            view -> Comparator.comparingLong(resource -> resource.getAmount(view))
        );
    }

    @Test
    void shouldAddResourcesWithSameNameButDifferentIdentity() {
        // Ensure that we do not get in trouble when adding 2 resources with the same name, but a different identity.
        // This test avoids the bug where the view insertion fails, because the resource is already "contained"
        // in the view, but actually isn't because it has a different identity.

        // Arrange
        final GridViewBuilder builder = getViewBuilder(
            (resource, craftable) -> Optional.of(new GridResourceWithMetadata(resource))
        );
        final GridView view = builder.build();

        // Act
        view.onChange(new ResourceWithMetadata(A, 1), 1, null);
        view.onChange(new ResourceWithMetadata(A, 2), 1, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceWithMetadata(new ResourceWithMetadata(A, 1)),
            new GridResourceWithMetadata(new ResourceWithMetadata(A, 2))
        );
    }

    @Test
    void shouldPreserveOrderWhenSortingAndTwoResourcesHaveTheSameQuantity() {
        // Arrange
        final GridView view = viewBuilder.build();
        view.setSortingDirection(GridSortingDirection.DESCENDING);

        // Act & assert
        view.onChange(A, 10, null);
        view.onChange(A, 5, null);
        view.onChange(B, 15, null);
        view.onChange(C, 2, null);

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(B),
            new GridResourceImpl(A),
            new GridResourceImpl(C)
        );

        view.onChange(A, -15, null);
        view.onChange(A, 15, null);

        view.onChange(B, -15, null);
        view.onChange(B, 15, null);

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(B),
            new GridResourceImpl(A),
            new GridResourceImpl(C)
        );
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void shouldLoadResourcesAndRetrieveTrackedResourcesProperly() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 1, new TrackedResource("Raoul", 1))
            .withResource(A, 1, new TrackedResource("RaoulA", 2))
            .withResource(B, 1, new TrackedResource("VDB", 3))
            .withResource(B, 1, null)
            .withResource(D, 1, null)
            .build();

        // Act
        final Optional<TrackedResource> a = view.getTrackedResource(A);
        final Optional<TrackedResource> b = view.getTrackedResource(B);
        final Optional<TrackedResource> d = view.getTrackedResource(D);
        final ResourceList backingList = view.copyBackingList();

        // Assert
        assertThat(a).get().usingRecursiveComparison().isEqualTo(new TrackedResource("RaoulA", 2));
        assertThat(b).isEmpty();
        assertThat(d).isEmpty();
        assertThat(backingList.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 2),
            new ResourceAmount(B, 2),
            new ResourceAmount(D, 1)
        );
        assertThat(view.getAmount(A)).isEqualTo(2);
        assertThat(view.getAmount(B)).isEqualTo(2);
        assertThat(view.getAmount(C)).isZero();
        assertThat(view.getAmount(D)).isEqualTo(1);
    }

    @Test
    void shouldInsertNewResource() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 15, null)
            .withResource(D, 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(A, 12, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(D, 10),
                new ResourceAmount(A, 12),
                new ResourceAmount(B, 15)
            );
        verify(listener, times(1)).run();
        assertThat(view.getAmount(A)).isEqualTo(12);
        assertThat(view.getAmount(B)).isEqualTo(15);
        assertThat(view.getAmount(D)).isEqualTo(10);
    }

    @Test
    void shouldSetFilterAndSort() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 10, null)
            .withResource(B, 10, null)
            .build();

        final BiPredicate<GridView, GridResource> filterA = (v, resource) -> resource.getName().equals(A.name());
        final BiPredicate<GridView, GridResource> filterB = (v, resource) -> resource.getName().equals(B.name());

        // Act
        final BiPredicate<GridView, GridResource> previousFilter1 = view.setFilterAndSort(filterA);
        final BiPredicate<GridView, GridResource> previousFilter2 = view.setFilterAndSort(filterB);

        // Assert
        assertThat(previousFilter1).isNotNull();
        assertThat(previousFilter2).isEqualTo(filterA);
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(B)
        );
        assertThat(view.getAmount(A)).isEqualTo(10);
        assertThat(view.getAmount(B)).isEqualTo(10);
    }

    @Test
    void shouldNotInsertNewResourceWhenFilteringProhibitsIt() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 15, null)
            .withResource(D, 10, null)
            .build();

        view.setFilterAndSort((v, resource) -> !resource.getName().equals(A.name()));

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(A, 12, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(B)
        );
        verify(listener, never()).run();
        assertThat(view.getAmount(A)).isEqualTo(12);
        assertThat(view.getAmount(B)).isEqualTo(15);
        assertThat(view.getAmount(D)).isEqualTo(10);
    }

    @Test
    void shouldCallListenerWhenSorting() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 6, null)
            .withResource(A, 15, null)
            .withResource(D, 10, null)
            .build();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.sort();

        // Assert
        verify(listener, times(1)).run();
        verifyNoMoreInteractions(listener);
    }

    @Test
    void shouldUpdateExistingResource() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 6, null)
            .withResource(A, 15, null)
            .withResource(D, 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(B, 5, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(B),
            new GridResourceImpl(A)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(D, 10),
                new ResourceAmount(B, 11),
                new ResourceAmount(A, 15)
            );
        verify(listener, times(1)).run();
        assertThat(view.getAmount(A)).isEqualTo(15);
        assertThat(view.getAmount(B)).isEqualTo(11);
        assertThat(view.getAmount(D)).isEqualTo(10);
    }

    @Test
    void shouldNotUpdateExistingResourceWhenFilteringProhibitsIt() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 6, null)
            .withResource(A, 15, null)
            .withResource(D, 10, null)
            .build();

        view.setFilterAndSort((v, resource) -> !resource.getName().equals(B.name()));

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(B, 5, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(B, 11),
                new ResourceAmount(D, 10),
                new ResourceAmount(A, 15)
            );
        verify(listener, never()).run();
        assertThat(view.getAmount(A)).isEqualTo(15);
        assertThat(view.getAmount(B)).isEqualTo(11);
        assertThat(view.getAmount(D)).isEqualTo(10);
    }

    @Test
    void shouldNotReorderExistingResourceWhenPreventingSorting() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 6, null)
            .withResource(A, 15, null)
            .withResource(D, 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(B),
            new GridResourceImpl(D),
            new GridResourceImpl(A)
        );

        final boolean changed = view.setPreventSorting(true);
        assertThat(changed).isTrue();
        final boolean changed2 = view.setPreventSorting(true);
        assertThat(changed2).isFalse();

        view.onChange(B, 5, null);
        verify(listener, never()).run();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(B),
            new GridResourceImpl(D),
            new GridResourceImpl(A)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(B, 11),
                new ResourceAmount(D, 10),
                new ResourceAmount(A, 15)
            );

        final boolean changed3 = view.setPreventSorting(false);
        assertThat(changed3).isTrue();
        view.sort();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(B),
            new GridResourceImpl(A)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(B, 11),
                new ResourceAmount(D, 10),
                new ResourceAmount(A, 15)
            );
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void shouldUpdateTrackedResourceAfterReceivingChange() {
        // Act
        final GridView view = viewBuilder.build();

        view.onChange(A, 1, new TrackedResource("Raoul", 1));
        view.onChange(A, 1, new TrackedResource("RaoulA", 2));

        view.onChange(B, 1, new TrackedResource("VDB", 3));
        view.onChange(B, 1, null);

        view.onChange(D, 1, null);

        // Assert
        final Optional<TrackedResource> a = view.getTrackedResource(A);
        final Optional<TrackedResource> b = view.getTrackedResource(B);
        final Optional<TrackedResource> c = view.getTrackedResource(D);

        assertThat(a).get().usingRecursiveComparison().isEqualTo(new TrackedResource("RaoulA", 2));
        assertThat(b).isEmpty();
        assertThat(c).isEmpty();
    }

    @Test
    void shouldUpdateExistingResourceWhenPerformingPartialRemoval() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 20, null)
            .withResource(A, 15, null)
            .withResource(D, 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(B, -7, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(B),
            new GridResourceImpl(A)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(D, 10),
                new ResourceAmount(B, 13),
                new ResourceAmount(A, 15)
            );
        verify(listener, times(1)).run();
        assertThat(view.getAmount(A)).isEqualTo(15);
        assertThat(view.getAmount(B)).isEqualTo(13);
        assertThat(view.getAmount(D)).isEqualTo(10);
    }

    @Test
    void shouldNotUpdateExistingResourceWhenPerformingPartialRemovalAndFilteringProhibitsIt() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 20, null)
            .withResource(A, 15, null)
            .withResource(D, 10, null)
            .build();

        view.setFilterAndSort((v, resource) -> !resource.getName().equals(B.name()));

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(B, -7, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A)
        );
        verify(listener, never()).run();
        assertThat(view.getAmount(A)).isEqualTo(15);
        assertThat(view.getAmount(B)).isEqualTo(13);
        assertThat(view.getAmount(D)).isEqualTo(10);
    }

    @Test
    void shouldNotReorderExistingResourceWhenPerformingPartialRemovalAndPreventingSorting() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 20, null)
            .withResource(A, 15, null)
            .withResource(D, 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );

        view.setPreventSorting(true);

        view.onChange(B, -7, null);
        verify(listener, never()).run();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );

        view.setPreventSorting(false);
        view.sort();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(B),
            new GridResourceImpl(A)
        );
    }

    @Test
    void shouldRemoveExistingResourceCompletely() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(B, 20, null)
            .withResource(A, 15, null)
            .withResource(D, 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act
        view.onChange(B, -20, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(D, 10),
                new ResourceAmount(A, 15)
            );
        verify(listener, times(1)).run();
        assertThat(view.getAmount(A)).isEqualTo(15);
        assertThat(view.getAmount(B)).isZero();
        assertThat(view.getAmount(D)).isEqualTo(10);
    }

    @Test
    void shouldNotReorderWhenRemovingExistingResourceCompletelyAndPreventingSorting() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 15, null)
            .withResource(B, 20, null)
            .withResource(D, 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );

        view.setPreventSorting(true);
        view.onChange(B, -20, null);
        verify(listener, never()).run();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(D, 10),
                new ResourceAmount(A, 15)
            );

        view.setPreventSorting(false);
        view.sort();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A)
        );
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new ResourceAmount(D, 10),
                new ResourceAmount(A, 15)
            );
    }

    @Test
    void shouldReuseExistingResourceWhenPreventingSortingAndRemovingExistingResourceCompletelyAndThenReinserting() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 15, null)
            .withResource(B, 20, null)
            .withResource(D, 10, null)
            .build();

        view.sort();

        final Runnable listener = mock(Runnable.class);
        view.setListener(listener);

        // Act & assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );

        // Delete the item
        view.setPreventSorting(true);
        view.onChange(B, -20, null);
        verify(listener, never()).run();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );

        // Re-insert the item
        view.onChange(B, 5, null);
        verify(listener, never()).run();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );

        // Re-insert the item again
        view.onChange(B, 3, null);
        verify(listener, never()).run();

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(D),
            new GridResourceImpl(A),
            new GridResourceImpl(B)
        );
    }

    @Test
    void shouldClear() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 15, new TrackedResource("Source", 0))
            .withResource(B, 20, new TrackedResource("Source", 0))
            .withResource(D, 10, new TrackedResource("Source", 0))
            .build();

        // Act
        view.clear();

        // Assert
        assertThat(view.getViewList()).isEmpty();
        assertThat(view.copyBackingList().copyState()).isEmpty();
        assertThat(view.getTrackedResource(A)).isEmpty();
        assertThat(view.getTrackedResource(B)).isEmpty();
        assertThat(view.getTrackedResource(D)).isEmpty();
        assertThat(view.getAmount(A)).isZero();
        assertThat(view.getAmount(B)).isZero();
        assertThat(view.getAmount(D)).isZero();
    }

    @Test
    void shouldIncludeAutocraftableResourceInViewList() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 15, null)
            .withAutocraftableResource(B)
            .build();

        // Act
        view.sort();

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new GridResourceImpl(A),
            new GridResourceImpl(B).autocraftable()
        );
        assertThat(view.isAutocraftable(A)).isFalse();
        assertThat(view.isAutocraftable(B)).isTrue();
        assertThat(view.getAmount(A)).isEqualTo(15);
        assertThat(view.getAmount(B)).isZero();
        assertThat(view.copyBackingList().copyState()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 15));
    }

    @Test
    void shouldIncludeAutocraftableResourceInViewListEvenIfItIsInTheBackingList() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 15, null)
            .withAutocraftableResource(A)
            .build();

        // Act
        view.sort();

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(A).autocraftable()
        );
        assertThat(view.isAutocraftable(A)).isTrue();
        assertThat(view.getAmount(A)).isEqualTo(15);
        assertThat(view.copyBackingList().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 15));
    }

    @Test
    void shouldNotRemoveAutocraftableResource() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 15, null)
            .withAutocraftableResource(A)
            .build();

        view.sort();

        // Act
        view.onChange(A, -15, null);

        // Assert
        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(A).autocraftable()
        );
        assertThat(view.isAutocraftable(A)).isTrue();
        assertThat(view.getAmount(A)).isZero();
        assertThat(view.copyBackingList().copyState()).isEmpty();
    }

    @Test
    void shouldNotRemoveAutocraftableResourceEvenWhenPreventingSorting() {
        // Arrange
        final GridView view = viewBuilder
            .withResource(A, 15, null)
            .withAutocraftableResource(A)
            .build();

        view.sort();
        view.setPreventSorting(true);

        // Act & assert
        view.onChange(A, -15, null);

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(A).autocraftable()
        );
        assertThat(view.isAutocraftable(A)).isTrue();
        assertThat(view.getAmount(A)).isZero();
        assertThat(view.copyBackingList().copyState()).isEmpty();

        view.onChange(A, 1, null);

        assertThat(view.getViewList()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new GridResourceImpl(A).autocraftable()
        );
        assertThat(view.isAutocraftable(A)).isTrue();
        assertThat(view.getAmount(A)).isEqualTo(1);
        assertThat(view.copyBackingList().copyState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );
    }

    private record ResourceWithMetadata(ResourceKey resource, int metadata) implements ResourceKey {
    }

    private static class GridResourceWithMetadata extends GridResourceImpl {
        GridResourceWithMetadata(final ResourceKey resource) {
            super(resource);
        }
    }
}
