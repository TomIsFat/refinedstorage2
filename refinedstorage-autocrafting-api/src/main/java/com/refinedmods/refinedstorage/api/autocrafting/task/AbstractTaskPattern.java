package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractTaskPattern {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTaskPattern.class);

    protected final Pattern pattern;
    protected final Map<Integer, Map<ResourceKey, Long>> ingredients = new HashMap<>();

    protected AbstractTaskPattern(final Pattern pattern, final TaskPlan.PatternPlan plan) {
        this.pattern = pattern;
        for (final Map.Entry<Integer, Map<ResourceKey, Long>> entry : plan.ingredients().entrySet()) {
            final Map<ResourceKey, Long> possibilitiesCopy = new LinkedHashMap<>(entry.getValue());
            ingredients.put(entry.getKey(), possibilitiesCopy);
        }
    }

    abstract boolean step(MutableResourceList internalStorage, ExternalPatternInputSink externalPatternInputSink);

    abstract long interceptInsertion(ResourceKey resource, long amount);

    protected final boolean extractAll(final ResourceList inputs,
                                       final MutableResourceList internalStorage,
                                       final Action action) {
        for (final ResourceKey inputResource : inputs.getAll()) {
            final long inputAmount = inputs.get(inputResource);
            final long inInternalStorage = internalStorage.get(inputResource);
            if (inInternalStorage < inputAmount) {
                return false;
            }
            if (action == Action.EXECUTE) {
                internalStorage.remove(inputResource, inputAmount);
                LOGGER.info("Extracted {}x {} from internal storage", inputAmount, inputResource);
            }
        }
        return true;
    }

    protected final ResourceList calculateIterationInputs(final Action action) {
        final MutableResourceList iterationInputs = MutableResourceListImpl.create();
        for (final Map.Entry<Integer, Map<ResourceKey, Long>> ingredient : ingredients.entrySet()) {
            final int ingredientIndex = ingredient.getKey();
            if (!calculateIterationInputs(ingredient, ingredientIndex, iterationInputs, action)) {
                throw new IllegalStateException();
            }
        }
        return iterationInputs;
    }

    private boolean calculateIterationInputs(final Map.Entry<Integer, Map<ResourceKey, Long>> ingredient,
                                             final int ingredientIndex,
                                             final MutableResourceList iterationInputs,
                                             final Action action) {
        long needed = pattern.ingredients().get(ingredientIndex).amount();
        for (final Map.Entry<ResourceKey, Long> possibility : ingredient.getValue().entrySet()) {
            final long available = Math.min(needed, possibility.getValue());
            if (available == 0) {
                continue;
            }
            iterationInputs.add(possibility.getKey(), available);
            if (action == Action.EXECUTE) {
                possibility.setValue(possibility.getValue() - available);
            }
            needed -= available;
            if (needed == 0) {
                break;
            }
        }
        return needed == 0;
    }
}
