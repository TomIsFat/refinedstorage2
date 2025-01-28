package com.refinedmods.refinedstorage.api.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageImplTest {
    private final Storage sut = new StorageImpl();

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldInsertResource(final Action action) {
        // Act
        final long inserted = sut.insert(TestResource.A, 64, action, Actor.EMPTY);

        // Assert
        assertThat(inserted).isEqualTo(64);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(TestResource.A, 64)
            );
            assertThat(sut.getStored()).isEqualTo(64);
        } else {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        }
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotInsertInvalidResourceOrAmount() {
        // Act
        final Executable action1 = () -> sut.insert(TestResource.A, 0, Action.EXECUTE, Actor.EMPTY);
        final Executable action2 = () -> sut.insert(TestResource.A, -1, Action.EXECUTE, Actor.EMPTY);
        final Executable action3 = () -> sut.insert(null, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }

    @Test
    void shouldNotExtractNonexistentResource() {
        // Act
        final long extracted = sut.extract(TestResource.A, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getStored()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractResourcePartly(final Action action) {
        // Arrange
        sut.insert(TestResource.A, 32, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long extracted = sut.extract(TestResource.A, 2, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(2);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(TestResource.A, 30)
            );
            assertThat(sut.getStored()).isEqualTo(30);
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(TestResource.A, 32)
            );
            assertThat(sut.getStored()).isEqualTo(32);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractResourceCompletely(final Action action) {
        // Arrange
        sut.insert(TestResource.A, 32, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long extracted = sut.extract(TestResource.A, 32, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(32);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(TestResource.A, 32)
            );
            assertThat(sut.getStored()).isEqualTo(32);
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotExtractMoreThanIsAvailable(final Action action) {
        // Arrange
        sut.insert(TestResource.A, 32, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long extracted = sut.extract(TestResource.A, 33, action, Actor.EMPTY);

        // Assert
        assertThat(extracted).isEqualTo(32);

        if (action == Action.EXECUTE) {
            assertThat(sut.getAll()).isEmpty();
            assertThat(sut.getStored()).isZero();
        } else {
            assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(TestResource.A, 32)
            );
            assertThat(sut.getStored()).isEqualTo(32);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void shouldNotExtractInvalidResourceOrAmount() {
        // Act
        final Executable action1 = () -> sut.extract(TestResource.A, 0, Action.EXECUTE, Actor.EMPTY);
        final Executable action2 = () -> sut.extract(TestResource.A, -1, Action.EXECUTE, Actor.EMPTY);
        final Executable action3 = () -> sut.extract(null, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }
}
