package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PatternTest {
    @Test
    void testPattern() {
        // Act
        final Pattern sut = new Pattern(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            PatternType.INTERNAL
        );

        // Assert
        assertThat(sut.ingredients()).hasSize(2);
        final Ingredient firstIngredient = sut.ingredients().getFirst();
        assertThat(firstIngredient.amount()).isEqualTo(1);
        assertThat(firstIngredient.inputs()).containsExactly(A, B);
        final Ingredient secondIngredient = sut.ingredients().get(1);
        assertThat(secondIngredient.amount()).isEqualTo(2);
        assertThat(secondIngredient.inputs()).containsExactly(C);
        assertThat(sut.outputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 3),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        assertThat(sut.type()).isEqualTo(PatternType.INTERNAL);
    }

    @Test
    void shouldNotCreatePatternWithoutIngredients() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(
            List.of(),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            PatternType.INTERNAL
        );

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCreatePatternWithoutOutputs() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(),
            PatternType.INTERNAL
        );

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCopyIngredientsAndOutputs() {
        // Arrange
        final List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(1, List.of(A, B)));
        final List<ResourceAmount> outputs = new ArrayList<>();
        outputs.add(new ResourceAmount(OAK_LOG, 3));
        final Pattern sut = new Pattern(ingredients, outputs, PatternType.INTERNAL);

        // Act
        ingredients.add(new Ingredient(2, List.of(C)));
        outputs.add(new ResourceAmount(OAK_PLANKS, 4));

        // Assert
        assertThat(sut.ingredients()).hasSize(1);
        assertThat(sut.outputs()).hasSize(1);
    }

    @Test
    void shouldNotBeAbleToModifyIngredientsAndOutputs() {
        // Arrange
        final Pattern sut = new Pattern(
            List.of(new Ingredient(1, List.of(A))),
            List.of(new ResourceAmount(OAK_LOG, 3)),
            PatternType.INTERNAL
        );
        final List<Ingredient> ingredients = sut.ingredients();
        final List<ResourceAmount> outputs = sut.outputs();

        final Ingredient newIngredient = new Ingredient(2, List.of(B));
        final ResourceAmount newOutput = new ResourceAmount(OAK_PLANKS, 4);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> ingredients.add(newIngredient);
        final ThrowableAssert.ThrowingCallable action2 = () -> outputs.add(newOutput);

        // Assert
        assertThatThrownBy(action).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(action2).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotCreatePatternWithoutPatternType() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(
            List.of(
                new Ingredient(1, List.of(A, B)),
                new Ingredient(2, List.of(C))
            ),
            List.of(
                new ResourceAmount(OAK_LOG, 3),
                new ResourceAmount(OAK_PLANKS, 4)
            ),
            null
        );

        // Assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }
}
