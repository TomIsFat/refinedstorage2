package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.PatternCycleDetectedException;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewCraftingCalculatorListener
    implements CraftingCalculatorListener<PreviewCraftingCalculatorListener.PreviewState> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewCraftingCalculatorListener.class);

    private final UUID listenerId = UUID.randomUUID();
    private PreviewState previewState;

    private PreviewCraftingCalculatorListener(final PreviewState previewState) {
        LOGGER.debug("{} - Started calculation", listenerId);
        this.previewState = previewState;
    }

    public static Preview calculatePreview(final CraftingCalculator calculator,
                                           final ResourceKey resource,
                                           final long amount) {
        final PreviewCraftingCalculatorListener listener = new PreviewCraftingCalculatorListener(new PreviewState());
        try {
            calculator.calculate(resource, amount, listener);
        } catch (final PatternCycleDetectedException e) {
            return new Preview(PreviewType.CYCLE_DETECTED, Collections.emptyList(), e.getPattern().getOutputs());
        }
        return listener.buildPreview();
    }

    @Override
    public CraftingCalculatorListener<PreviewState> childCalculationStarted() {
        return new PreviewCraftingCalculatorListener(previewState.copy());
    }

    @Override
    public void childCalculationCompleted(final ResourceKey resource,
                                          final long amount,
                                          final CraftingCalculatorListener<PreviewState> childListener) {
        LOGGER.debug("{} - Child calculation completed for {}x {}", listenerId, amount, resource);
        this.previewState = ((PreviewCraftingCalculatorListener) childListener).previewState;
        this.previewState.toCraft.add(resource, amount);
        if (LOGGER.isDebugEnabled()) {
            for (final ResourceKey missingResource : previewState.missing.getAll()) {
                LOGGER.debug("{} - Missing: {}x {}", listenerId, previewState.missing.get(missingResource),
                    missingResource);
            }
            for (final ResourceKey toCraftResource : previewState.toCraft.getAll()) {
                LOGGER.debug("{} - To craft: {}x {}", listenerId, previewState.toCraft.get(toCraftResource),
                    toCraftResource);
            }
            for (final ResourceKey toTakeFromStorageResource : previewState.toTakeFromStorage.getAll()) {
                LOGGER.debug("{} - To take from storage: {}x {}", listenerId,
                    previewState.toTakeFromStorage.get(toTakeFromStorageResource), toTakeFromStorageResource);
            }
        }
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        LOGGER.debug("{} - Ingredients exhausted for {}x {}", listenerId, amount, resource);
        previewState.missing.add(resource, amount);
    }

    @Override
    public void ingredientExtractedFromStorage(final ResourceKey resource, final long amount) {
        LOGGER.debug("{} - Extracted from storage: {} - {}", listenerId, resource, amount);
        previewState.toTakeFromStorage.add(resource, amount);
    }

    public Preview buildPreview() {
        final PreviewType previewType = previewState.missing.getAll().isEmpty()
            ? PreviewType.SUCCESS
            : PreviewType.MISSING_RESOURCES;
        final PreviewBuilder previewBuilder = PreviewBuilder.ofType(previewType);
        previewState.missing.getAll().forEach(resource ->
            previewBuilder.addMissing(resource, previewState.missing.get(resource)));
        previewState.toCraft.getAll().forEach(resource ->
            previewBuilder.addToCraft(resource, previewState.toCraft.get(resource)));
        previewState.toTakeFromStorage.getAll().forEach(resource ->
            previewBuilder.addAvailable(resource, previewState.toTakeFromStorage.get(resource)));
        return previewBuilder.build();
    }

    public record PreviewState(MutableResourceList toTakeFromStorage,
                               MutableResourceList toCraft,
                               MutableResourceList missing) {
        private PreviewState() {
            this(MutableResourceListImpl.create(), MutableResourceListImpl.create(), MutableResourceListImpl.create());
        }

        private PreviewState copy() {
            return new PreviewState(
                toTakeFromStorage.copy(),
                toCraft.copy(),
                missing.copy()
            );
        }
    }
}
