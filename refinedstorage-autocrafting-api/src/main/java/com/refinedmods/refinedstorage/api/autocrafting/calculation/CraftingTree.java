package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

class CraftingTree<T> {
    private final Pattern pattern;
    private final Amount amount;
    private final PatternRepository patternRepository;
    private final CraftingCalculatorListener<T> listener;
    private final Set<Pattern> activePatterns;
    private CraftingState craftingState;

    private CraftingTree(final Pattern pattern,
                         final CraftingState craftingState,
                         final Amount amount,
                         final PatternRepository patternRepository,
                         final CraftingCalculatorListener<T> listener,
                         final Set<Pattern> activePatterns) {
        this.pattern = pattern;
        this.craftingState = craftingState;
        this.amount = amount;
        this.patternRepository = patternRepository;
        this.listener = listener;
        this.activePatterns = activePatterns;
    }

    static <T> CraftingTree<T> root(final Pattern pattern,
                                    final RootStorage rootStorage,
                                    final Amount amount,
                                    final PatternRepository patternRepository,
                                    final CraftingCalculatorListener<T> listener) {
        final CraftingState craftingState = CraftingState.of(rootStorage);
        return new CraftingTree<>(pattern, craftingState, amount, patternRepository, listener, new HashSet<>());
    }

    static <T> CraftingTree<T> child(final Pattern pattern,
                                     final CraftingState parentState,
                                     final ResourceKey resource,
                                     final Amount amount,
                                     final PatternRepository patternRepository,
                                     final CraftingCalculatorListener<T> listener,
                                     final Set<Pattern> activePatterns) {
        final CraftingCalculatorListener<T> childListener = listener.childCalculationStarted(
            pattern,
            resource,
            amount
        );
        final CraftingState childState = parentState.copy();
        return new CraftingTree<>(pattern, childState, amount, patternRepository, childListener, activePatterns);
    }

    CalculationResult calculate() {
        if (!activePatterns.add(pattern)) {
            throw new PatternCycleDetectedException(pattern);
        }
        CalculationResult result = CalculationResult.SUCCESS;
        final List<Ingredient> ingredients = pattern.layout().ingredients();
        for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ++ingredientIndex) {
            final Ingredient ingredient = ingredients.get(ingredientIndex);
            final IngredientState ingredientState = new IngredientState(ingredient, craftingState);
            final CalculationResult ingredientResult = calculateIngredient(ingredientIndex, ingredientState);
            if (ingredientResult == CalculationResult.MISSING_RESOURCES) {
                result = CalculationResult.MISSING_RESOURCES;
            }
        }
        craftingState.addOutputsToInternalStorage(pattern, amount);
        activePatterns.remove(pattern);
        return result;
    }

    private CalculationResult calculateIngredient(final int ingredientIndex, final IngredientState ingredientState) {
        CraftingState.ResourceState resourceState = craftingState.getResource(ingredientState.get());
        long remaining = ingredientState.amount() * amount.iterations();
        if (remaining < 0) {
            throw new NumberOverflowDuringCalculationException();
        }
        while (remaining > 0) {
            if (resourceState.isInInternalStorage()) {
                final long toTake = Math.min(remaining, resourceState.inInternalStorage());
                craftingState.extractFromInternalStorage(resourceState.resource(), toTake);
                listener.ingredientUsed(pattern, ingredientIndex, resourceState.resource(), toTake);
                remaining -= toTake;
            }
            if (remaining > 0 && resourceState.isInStorage()) {
                final long toTake = Math.min(remaining, resourceState.inStorage());
                craftingState.extractFromStorage(resourceState.resource(), toTake);
                listener.ingredientExtractedFromStorage(resourceState.resource(), toTake);
                listener.ingredientUsed(pattern, ingredientIndex, resourceState.resource(), toTake);
                remaining -= toTake;
            }
            if (remaining > 0) {
                final CraftingState.ResourceState newState = tryCalculateChild(
                    ingredientState,
                    resourceState,
                    remaining
                );
                if (newState == null) {
                    craftingState.extractFromInternalStorage(resourceState.resource(), remaining);
                    return CalculationResult.MISSING_RESOURCES;
                } else {
                    resourceState = newState;
                }
            }
        }
        return CalculationResult.SUCCESS;
    }

    @Nullable
    private CraftingState.ResourceState tryCalculateChild(final IngredientState ingredientState,
                                                          final CraftingState.ResourceState resourceState,
                                                          final long remaining) {
        final Collection<Pattern> childPatterns = patternRepository.getByOutput(resourceState.resource());
        if (!childPatterns.isEmpty()) {
            return calculateChild(
                ingredientState,
                remaining,
                childPatterns,
                resourceState
            );
        }
        return ingredientState.cycle().map(craftingState::getResource).orElseGet(() -> {
            listener.ingredientsExhausted(resourceState.resource(), remaining);
            return null;
        });
    }

    @Nullable
    private CraftingState.ResourceState calculateChild(final IngredientState ingredientState,
                                                       final long remaining,
                                                       final Collection<Pattern> childPatterns,
                                                       final CraftingState.ResourceState resourceState) {
        final ChildCalculationResult<T> result = calculateChild(remaining, childPatterns, resourceState);
        if (result.success) {
            this.craftingState = result.childTree.craftingState;
            final CraftingState.ResourceState updatedResourceState = craftingState.getResource(
                resourceState.resource()
            );
            listener.childCalculationCompleted(result.childTree.listener);
            return updatedResourceState;
        }
        return cycleToNextIngredientOrFail(ingredientState, resourceState, result);
    }

    private ChildCalculationResult<T> calculateChild(final long remaining,
                                                     final Collection<Pattern> childPatterns,
                                                     final CraftingState.ResourceState resourceState) {
        CraftingTree<T> lastChildTree = null;
        for (final Pattern childPattern : childPatterns) {
            final Amount childAmount = Amount.of(childPattern, resourceState.resource(), remaining);
            final CraftingTree<T> childTree = child(
                childPattern,
                craftingState,
                resourceState.resource(),
                childAmount,
                patternRepository,
                listener,
                activePatterns
            );
            final CalculationResult childResult = childTree.calculate();
            if (childResult == CalculationResult.MISSING_RESOURCES) {
                lastChildTree = childTree;
                continue;
            }
            return new ChildCalculationResult<>(true, childTree);
        }
        return new ChildCalculationResult<>(false, requireNonNull(lastChildTree));
    }

    @Nullable
    private CraftingState.ResourceState cycleToNextIngredientOrFail(final IngredientState ingredientState,
                                                                    final CraftingState.ResourceState resourceState,
                                                                    final ChildCalculationResult<T> childResult) {
        return ingredientState.cycle().map(craftingState::getResource).orElseGet(() -> {
            this.craftingState = childResult.childTree.craftingState;
            listener.childCalculationCompleted(childResult.childTree.listener);
            return null;
        });
    }

    private record ChildCalculationResult<T>(boolean success, CraftingTree<T> childTree) {
    }

    enum CalculationResult {
        SUCCESS,
        MISSING_RESOURCES
    }
}
