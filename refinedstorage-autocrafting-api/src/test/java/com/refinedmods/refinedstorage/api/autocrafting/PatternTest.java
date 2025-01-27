package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            UUID.randomUUID(),
            new PatternLayout(
                List.of(
                    new Ingredient(1, List.of(A, B)),
                    new Ingredient(2, List.of(C))
                ),
                List.of(
                    new ResourceAmount(OAK_LOG, 3),
                    new ResourceAmount(OAK_PLANKS, 4)
                ),
                PatternType.INTERNAL
            )
        );

        // Assert
        assertThat(sut.layout().ingredients()).hasSize(2);
        final Ingredient firstIngredient = sut.layout().ingredients().getFirst();
        assertThat(firstIngredient.amount()).isEqualTo(1);
        assertThat(firstIngredient.inputs()).containsExactly(A, B);
        final Ingredient secondIngredient = sut.layout().ingredients().get(1);
        assertThat(secondIngredient.amount()).isEqualTo(2);
        assertThat(secondIngredient.inputs()).containsExactly(C);
        assertThat(sut.layout().outputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_LOG, 3),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        assertThat(sut.layout().type()).isEqualTo(PatternType.INTERNAL);
    }

    @Test
    void shouldNotCreatePatternWithoutIngredients() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(
            UUID.randomUUID(),
            new PatternLayout(
                List.of(),
                List.of(
                    new ResourceAmount(OAK_LOG, 3),
                    new ResourceAmount(OAK_PLANKS, 4)
                ),
                PatternType.INTERNAL
            )
        );

        // Assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCreatePatternWithoutOutputs() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(
            UUID.randomUUID(),
            new PatternLayout(
                List.of(
                    new Ingredient(1, List.of(A, B)),
                    new Ingredient(2, List.of(C))
                ),
                List.of(),
                PatternType.INTERNAL
            )
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
        final Pattern sut = new Pattern(
            UUID.randomUUID(),
            new PatternLayout(ingredients, outputs, PatternType.INTERNAL)
        );

        // Act
        ingredients.add(new Ingredient(2, List.of(C)));
        outputs.add(new ResourceAmount(OAK_PLANKS, 4));

        // Assert
        assertThat(sut.layout().ingredients()).hasSize(1);
        assertThat(sut.layout().outputs()).hasSize(1);
    }

    @Test
    void shouldNotBeAbleToModifyIngredientsAndOutputs() {
        // Arrange
        final Pattern sut = new Pattern(
            UUID.randomUUID(),
            new PatternLayout(
                List.of(new Ingredient(1, List.of(A))),
                List.of(new ResourceAmount(OAK_LOG, 3)),
                PatternType.INTERNAL
            )
        );
        final List<Ingredient> ingredients = sut.layout().ingredients();
        final List<ResourceAmount> outputs = sut.layout().outputs();

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
            UUID.randomUUID(),
            new PatternLayout(
                List.of(
                    new Ingredient(1, List.of(A, B)),
                    new Ingredient(2, List.of(C))
                ),
                List.of(
                    new ResourceAmount(OAK_LOG, 3),
                    new ResourceAmount(OAK_PLANKS, 4)
                ),
                null
            )
        );

        // Assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotCreateWithoutId() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> new Pattern(
            null,
            new PatternLayout(
                List.of(
                    new Ingredient(1, List.of(A, B)),
                    new Ingredient(2, List.of(C))
                ),
                List.of(
                    new ResourceAmount(OAK_LOG, 3),
                    new ResourceAmount(OAK_PLANKS, 4)
                ),
                PatternType.INTERNAL
            )
        );

        // Assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }
}
