package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingHelpers.patterns;
import static com.refinedmods.refinedstorage.api.autocrafting.AutocraftingHelpers.storage;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskCraftingCalculatorListener.calculatePlan;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.CRAFTING_TABLE_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.OAK_PLANKS_PATTERN;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskUtil.SPRUCE_PLANKS_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskPlanTest {
    @Test
    void shouldNotPlanTaskWhenThereAreMissingResources() {
        // Arrange
        final RootStorage storage = storage();
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN);
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Optional<TaskPlan> optionalPlan = calculatePlan(sut, OAK_PLANKS, 1);

        // Assert
        assertThat(optionalPlan).isEmpty();
    }

    @Test
    void testPlanTaskWithIngredientsUsedFromRootStorageAndInternalStorageWithChildPattern() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Optional<TaskPlan> optionalPlan = calculatePlan(sut, CRAFTING_TABLE, 3);

        // Assert
        assertThat(optionalPlan).isPresent();
        final TaskPlan plan = optionalPlan.get();

        assertThat(plan.initialRequirements()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(OAK_PLANKS, 4),
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1)
        );
        assertThat(plan.patterns()).containsOnlyKeys(CRAFTING_TABLE_PATTERN, OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN);
        assertThat(plan.pattern(CRAFTING_TABLE_PATTERN))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(3, Map.of(
                0, Map.of(OAK_PLANKS, 3L),
                1, Map.of(OAK_PLANKS, 3L),
                2, Map.of(OAK_PLANKS, 2L, SPRUCE_PLANKS, 1L),
                3, Map.of(SPRUCE_PLANKS, 3L)
            )));
        assertThat(plan.pattern(OAK_PLANKS_PATTERN))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(1, Map.of(
                0, Map.of(OAK_LOG, 1L)
            )));
        assertThat(plan.pattern(SPRUCE_PLANKS_PATTERN))
            .usingRecursiveComparison()
            .isEqualTo(new TaskPlan.PatternPlan(1, Map.of(
                0, Map.of(SPRUCE_LOG, 1L)
            )));
    }

    @Test
    void shouldNotModifyPlan() {
        // Arrange
        final RootStorage storage = storage(
            new ResourceAmount(OAK_LOG, 1),
            new ResourceAmount(SPRUCE_LOG, 1),
            new ResourceAmount(OAK_PLANKS, 4)
        );
        final PatternRepository patterns = patterns(OAK_PLANKS_PATTERN, SPRUCE_PLANKS_PATTERN, CRAFTING_TABLE_PATTERN);
        final CraftingCalculator sut = new CraftingCalculatorImpl(patterns, storage);

        // Act
        final Optional<TaskPlan> optionalPlan = calculatePlan(sut, CRAFTING_TABLE, 3);

        // Assert
        assertThat(optionalPlan).isPresent();
        final TaskPlan plan = optionalPlan.get();

        assertThatThrownBy(() -> plan.initialRequirements().add(new ResourceAmount(OAK_LOG, 1)))
            .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> plan.patterns().clear())
            .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> plan.pattern(CRAFTING_TABLE_PATTERN).ingredients().clear())
            .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> plan.pattern(CRAFTING_TABLE_PATTERN).ingredients().get(0).put(OAK_LOG, 1L))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
