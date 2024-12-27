package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.List;

import static com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingTree.root;

public class CraftingCalculatorImpl implements CraftingCalculator {
    private final PatternRepository patternRepository;
    private final RootStorage rootStorage;

    public CraftingCalculatorImpl(final PatternRepository patternRepository, final RootStorage rootStorage) {
        this.patternRepository = patternRepository;
        this.rootStorage = rootStorage;
    }

    @Override
    public <T> void calculate(final ResourceKey resource,
                              final long amount,
                              final CraftingCalculatorListener<T> listener) {
        CoreValidations.validateLargerThanZero(amount, "Requested amount must be greater than 0");
        final List<Pattern> patterns = patternRepository.getByOutput(resource);
        CraftingCalculatorListener<T> lastChildListener = null;
        Amount lastPatternAmount = null;
        for (final Pattern pattern : patterns) {
            final Amount patternAmount = Amount.of(pattern, resource, amount);
            if (patternAmount.getTotal() < 0) {
                throw new NumberOverflowDuringCalculationException();
            }
            final CraftingCalculatorListener<T> childListener = listener.childCalculationStarted();
            final CraftingTree<T> tree = root(pattern, rootStorage, patternAmount, patternRepository, childListener);
            final CraftingTree.CalculationResult calculationResult = tree.calculate();
            if (calculationResult == CraftingTree.CalculationResult.MISSING_RESOURCES) {
                lastChildListener = childListener;
                lastPatternAmount = patternAmount;
                continue;
            }
            listener.childCalculationCompleted(resource, patternAmount.getTotal(), childListener);
            return;
        }
        if (lastChildListener == null) {
            throw new IllegalStateException("No pattern found for " + resource);
        }
        listener.childCalculationCompleted(resource, lastPatternAmount.getTotal(), lastChildListener);
    }
}
