package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngredientTest {
    @Test
    void testIngredient() {
        // Act
        final Ingredient sut = new Ingredient(1, List.of(A, B));

        // Assert
        assertThat(sut.amount()).isEqualTo(1);
        assertThat(sut.inputs()).containsExactly(A, B);
    }

    @Test
    void shouldCopyIngredients() {
        // Arrange
        final List<ResourceKey> outputs = new ArrayList<>();
        outputs.add(A);

        // Act
        final Ingredient sut = new Ingredient(1, outputs);
        outputs.add(B);

        // Assert
        assertThat(sut.amount()).isEqualTo(1);
        assertThat(sut.inputs()).containsExactly(A);
    }

    @Test
    void shouldNotBeAbleToModifyIngredients() {
        // Arrange
        final Ingredient sut = new Ingredient(1, List.of(A));
        final List<ResourceKey> inputs = sut.inputs();

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> inputs.add(B);

        // Assert
        assertThatThrownBy(action).isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void shouldNotCreateIngredientWithInvalidAmount(final long amount) {
        // Arrange
        final ThrowableAssert.ThrowingCallable action = () -> new Ingredient(amount, List.of(A, B));

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCreateIngredientWithEmptyInputs() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Ingredient(1, List.of());

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }
}
