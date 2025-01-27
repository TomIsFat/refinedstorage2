package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.Amount;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.NumberOverflowDuringCalculationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.PatternCycleDetectedException;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewCraftingCalculatorListener implements CraftingCalculatorListener<PreviewBuilder> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewCraftingCalculatorListener.class);

    private final UUID listenerId = UUID.randomUUID();
    private PreviewBuilder builder;

    private PreviewCraftingCalculatorListener(final PreviewBuilder builder) {
        LOGGER.debug("{} - Started calculation", listenerId);
        this.builder = builder;
    }

    public static Preview calculatePreview(final CraftingCalculator calculator,
                                           final ResourceKey resource,
                                           final long amount) {
        final PreviewCraftingCalculatorListener listener = new PreviewCraftingCalculatorListener(
            PreviewBuilder.create()
        );
        try {
            calculator.calculate(resource, amount, listener);
        } catch (final PatternCycleDetectedException e) {
            return new Preview(PreviewType.CYCLE_DETECTED, Collections.emptyList(), e.getPattern().layout().outputs());
        } catch (final NumberOverflowDuringCalculationException e) {
            return new Preview(PreviewType.OVERFLOW, Collections.emptyList(), Collections.emptyList());
        }
        return listener.buildPreview();
    }

    @Override
    public CraftingCalculatorListener<PreviewBuilder> childCalculationStarted(final Pattern childPattern,
                                                                              final ResourceKey resource,
                                                                              final Amount amount) {
        LOGGER.debug("{} - Child calculation starting for {}x {}", listenerId, amount, resource);
        final PreviewBuilder copy = builder.copy();
        copy.addToCraft(resource, amount.getTotal());
        return new PreviewCraftingCalculatorListener(copy);
    }

    @Override
    public void childCalculationCompleted(final CraftingCalculatorListener<PreviewBuilder> childListener) {
        LOGGER.debug("{} - Child calculation completed", listenerId);
        this.builder = childListener.getData();
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        LOGGER.debug("{} - Ingredients exhausted for {}x {}", listenerId, amount, resource);
        builder.addMissing(resource, amount);
    }

    @Override
    public void ingredientUsed(final Pattern ingredientPattern,
                               final int ingredientIndex,
                               final ResourceKey resource,
                               final long amount) {
        // no op
    }

    @Override
    public void ingredientExtractedFromStorage(final ResourceKey resource, final long amount) {
        LOGGER.debug("{} - Extracted from storage: {} - {}", listenerId, resource, amount);
        builder.addAvailable(resource, amount);
    }

    @Override
    public PreviewBuilder getData() {
        return builder;
    }

    private Preview buildPreview() {
        return builder.build();
    }
}
