package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.TestResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class AbstractMutableResourceListTest {
    private MutableResourceList list;

    @BeforeEach
    void setUp() {
        list = createList();
    }

    protected abstract MutableResourceList createList();

    @Test
    void testInitialState() {
        // Assert
        assertThat(list.copyState()).isEmpty();
        assertThat(list.isEmpty()).isTrue();
    }

    @Test
    void shouldAddNewResource() {
        // Act
        final MutableResourceList.OperationResult result = list.add(TestResource.A, 10);

        // Assert
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.amount()).isEqualTo(10);
        assertThat(result.resource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isTrue();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(TestResource.A, 10)
        );
        assertThat(list.get(TestResource.A)).isEqualTo(10);
        assertThat(list.contains(TestResource.A)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactly(TestResource.A);
    }

    @Test
    void shouldAddNewResourceWithResourceAmountDirectly() {
        // Act
        final MutableResourceList.OperationResult result = list.add(new ResourceAmount(TestResource.A, 10));

        // Assert
        assertThat(result.change()).isEqualTo(10);
        assertThat(result.amount()).isEqualTo(10);
        assertThat(result.resource()).isEqualTo(TestResource.A);
        assertThat(result.available()).isTrue();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(TestResource.A, 10)
        );
        assertThat(list.get(TestResource.A)).isEqualTo(10);
        assertThat(list.contains(TestResource.A)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactly(TestResource.A);
    }

    @Test
    void shouldAddMultipleOfSameResource() {
        // Act
        final MutableResourceList.OperationResult result1 = list.add(TestResource.A, 10);
        final MutableResourceList.OperationResult result2 = list.add(TestResource.A, 5);

        // Assert
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.amount()).isEqualTo(10);
        assertThat(result1.resource()).isEqualTo(TestResource.A);
        assertThat(result1.available()).isTrue();

        assertThat(result2.change()).isEqualTo(5);
        assertThat(result2.amount()).isEqualTo(15);
        assertThat(result2.resource()).isEqualTo(TestResource.A);
        assertThat(result2.available()).isTrue();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(TestResource.A, 15)
        );
        assertThat(list.get(TestResource.A)).isEqualTo(15);
        assertThat(list.contains(TestResource.A)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactly(TestResource.A);
    }

    @Test
    void shouldAddMultipleOfDifferentResources() {
        // Act
        final MutableResourceList.OperationResult result1 = list.add(TestResource.A, 10);
        final MutableResourceList.OperationResult result2 = list.add(TestResource.A, 5);
        final MutableResourceList.OperationResult result3 = list.add(TestResource.B, 3);

        // Assert
        assertThat(result1.change()).isEqualTo(10);
        assertThat(result1.amount()).isEqualTo(10);
        assertThat(result1.resource()).isEqualTo(TestResource.A);
        assertThat(result1.available()).isTrue();

        assertThat(result2.change()).isEqualTo(5);
        assertThat(result2.amount()).isEqualTo(15);
        assertThat(result2.resource()).isEqualTo(TestResource.A);
        assertThat(result2.available()).isTrue();

        assertThat(result3.change()).isEqualTo(3);
        assertThat(result3.amount()).isEqualTo(3);
        assertThat(result3.resource()).isEqualTo(TestResource.B);
        assertThat(result3.available()).isTrue();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 15),
            new ResourceAmount(TestResource.B, 3)
        );
        assertThat(list.get(TestResource.A)).isEqualTo(15);
        assertThat(list.contains(TestResource.A)).isTrue();
        assertThat(list.get(TestResource.B)).isEqualTo(3);
        assertThat(list.contains(TestResource.B)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactlyInAnyOrder(TestResource.A, TestResource.B);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotAddInvalidResourceOrAmount() {
        // Act
        final Executable action1 = () -> list.add(TestResource.A, 0);
        final Executable action2 = () -> list.add(TestResource.A, -1);
        final Executable action3 = () -> list.add(null, 1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }

    @Test
    void shouldNotRemoveResourceWhenItIsNotAvailable() {
        // Act
        final Optional<MutableResourceList.OperationResult> result = list.remove(TestResource.A, 10);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldRemoveResourcePartly() {
        // Arrange
        list.add(TestResource.A, 20);
        list.add(TestResource.B, 6);

        // Act
        final Optional<MutableResourceList.OperationResult> result2 = list.remove(TestResource.A, 5);

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().change()).isEqualTo(-5);
        assertThat(result2.get().amount()).isEqualTo(15);
        assertThat(result2.get().resource()).isEqualTo(TestResource.A);
        assertThat(result2.get().available()).isTrue();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 15),
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(list.get(TestResource.A)).isEqualTo(15);
        assertThat(list.contains(TestResource.A)).isTrue();
        assertThat(list.get(TestResource.B)).isEqualTo(6);
        assertThat(list.contains(TestResource.B)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactlyInAnyOrder(TestResource.A, TestResource.B);
    }

    @Test
    void shouldRemoveResourcePartlyWithResourceAmount() {
        // Arrange
        list.add(TestResource.A, 20);
        list.add(TestResource.B, 6);

        // Act
        final Optional<MutableResourceList.OperationResult> result2 = list.remove(new ResourceAmount(
            TestResource.A,
            5
        ));

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().change()).isEqualTo(-5);
        assertThat(result2.get().amount()).isEqualTo(15);
        assertThat(result2.get().resource()).isEqualTo(TestResource.A);
        assertThat(result2.get().available()).isTrue();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 15),
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(list.get(TestResource.A)).isEqualTo(15);
        assertThat(list.contains(TestResource.A)).isTrue();
        assertThat(list.get(TestResource.B)).isEqualTo(6);
        assertThat(list.contains(TestResource.B)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactlyInAnyOrder(TestResource.A, TestResource.B);
    }

    @Test
    void shouldRemoveResourceCompletely() {
        // Arrange
        list.add(TestResource.A, 20);
        list.add(TestResource.B, 6);

        // Act
        final Optional<MutableResourceList.OperationResult> result = list.remove(TestResource.A, 20);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().change()).isEqualTo(-20);
        assertThat(result.get().amount()).isZero();
        assertThat(result.get().resource()).isEqualTo(TestResource.A);
        assertThat(result.get().available()).isFalse();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(list.get(TestResource.A)).isZero();
        assertThat(list.contains(TestResource.A)).isFalse();
        assertThat(list.get(TestResource.B)).isEqualTo(6);
        assertThat(list.contains(TestResource.B)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactly(TestResource.B);
    }

    @Test
    void shouldRemoveResourceCompletelyWithResourceAmount() {
        // Arrange
        list.add(TestResource.A, 20);
        list.add(TestResource.B, 6);

        // Act
        final Optional<MutableResourceList.OperationResult> result2 = list.remove(new ResourceAmount(
            TestResource.A,
            20
        ));

        // Assert
        assertThat(result2).isPresent();
        assertThat(result2.get().change()).isEqualTo(-20);
        assertThat(result2.get().amount()).isZero();
        assertThat(result2.get().resource()).isEqualTo(TestResource.A);
        assertThat(result2.get().available()).isFalse();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(list.get(TestResource.A)).isZero();
        assertThat(list.contains(TestResource.A)).isFalse();
        assertThat(list.get(TestResource.B)).isEqualTo(6);
        assertThat(list.contains(TestResource.B)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactly(TestResource.B);
    }

    @Test
    void shouldRemoveLastResourceOfResourceList() {
        // Arrange
        list.add(TestResource.A, 1);

        // Act
        final Optional<MutableResourceList.OperationResult> result = list.remove(TestResource.A, 1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().change()).isEqualTo(-1);
        assertThat(result.get().amount()).isZero();
        assertThat(result.get().resource()).isEqualTo(TestResource.A);
        assertThat(result.get().available()).isFalse();

        assertThat(list.copyState()).isEmpty();
        assertThat(list.get(TestResource.A)).isZero();
        assertThat(list.contains(TestResource.A)).isFalse();
        assertThat(list.isEmpty()).isTrue();
        assertThat(list.getAll()).isEmpty();
    }

    @Test
    void shouldNotRemoveResourceWithMoreThanIsAvailable() {
        // Arrange
        list.add(TestResource.A, 20);
        list.add(TestResource.B, 6);

        // Act
        final Optional<MutableResourceList.OperationResult> result = list.remove(TestResource.A, 21);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().change()).isEqualTo(-20);
        assertThat(result.get().amount()).isZero();
        assertThat(result.get().resource()).isEqualTo(TestResource.A);
        assertThat(result.get().available()).isFalse();

        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.B, 6)
        );

        assertThat(list.get(TestResource.A)).isZero();
        assertThat(list.contains(TestResource.A)).isFalse();
        assertThat(list.get(TestResource.B)).isEqualTo(6);
        assertThat(list.contains(TestResource.B)).isTrue();
        assertThat(list.isEmpty()).isFalse();
        assertThat(list.getAll()).containsExactly(TestResource.B);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotRemoveInvalidResourceOrAmount() {
        // Act
        final Executable action1 = () -> list.remove(TestResource.A, 0);
        final Executable action2 = () -> list.remove(TestResource.A, -1);
        final Executable action3 = () -> list.remove(null, 1);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }

    @Test
    void shouldClearList() {
        // Arrange
        list.add(TestResource.A, 10);
        list.add(TestResource.B, 5);

        final Collection<ResourceAmount> contentsBeforeClear = new ArrayList<>(list.copyState());

        // Act
        list.clear();

        // Assert
        final Collection<ResourceAmount> contentsAfterClear = list.copyState();

        assertThat(contentsBeforeClear).hasSize(2);
        assertThat(contentsAfterClear).isEmpty();

        assertThat(list.get(TestResource.A)).isZero();
        assertThat(list.get(TestResource.B)).isZero();
        assertThat(list.isEmpty()).isTrue();
        assertThat(list.getAll()).isEmpty();
    }

    @Test
    void shouldCopyList() {
        // Arrange
        list.add(TestResource.A, 10);
        list.add(TestResource.B, 5);

        // Act
        final MutableResourceList copy = list.copy();

        list.add(TestResource.A, 1);
        list.add(TestResource.C, 3);

        copy.add(TestResource.A, 2);
        copy.add(TestResource.D, 3);

        // Assert
        assertThat(list.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 11),
            new ResourceAmount(TestResource.B, 5),
            new ResourceAmount(TestResource.C, 3)
        );
        assertThat(copy.copyState()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(TestResource.A, 12),
            new ResourceAmount(TestResource.B, 5),
            new ResourceAmount(TestResource.D, 3)
        );
        assertThat(list.isEmpty()).isFalse();
        assertThat(copy.isEmpty()).isFalse();
    }
}
