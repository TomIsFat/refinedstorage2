package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

class MissingResourcesCraftingCalculatorListener implements CraftingCalculatorListener<Boolean> {
    private boolean missingResources;

    MissingResourcesCraftingCalculatorListener() {
    }

    MissingResourcesCraftingCalculatorListener(final boolean missingResources) {
        this.missingResources = missingResources;
    }

    boolean isMissingResources() {
        return missingResources;
    }

    @Override
    public CraftingCalculatorListener<Boolean> childCalculationStarted() {
        return new MissingResourcesCraftingCalculatorListener(missingResources);
    }

    @Override
    public void childCalculationCompleted(final ResourceKey resource,
                                          final long amount,
                                          final CraftingCalculatorListener<Boolean> childListener) {
        missingResources = ((MissingResourcesCraftingCalculatorListener) childListener).missingResources;
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        missingResources = true;
    }

    @Override
    public void ingredientExtractedFromStorage(final ResourceKey resource, final long amount) {
        // no op
    }
}
