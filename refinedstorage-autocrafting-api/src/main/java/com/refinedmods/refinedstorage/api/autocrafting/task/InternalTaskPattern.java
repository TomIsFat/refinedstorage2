package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InternalTaskPattern extends AbstractTaskPattern {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalTaskPattern.class);

    private long iterationsRemaining;

    InternalTaskPattern(final Pattern pattern, final TaskPlan.PatternPlan plan) {
        super(pattern, plan);
        this.iterationsRemaining = plan.iterations();
    }

    @Override
    boolean step(final MutableResourceList internalStorage,
                 final RootStorage rootStorage,
                 final ExternalPatternInputSink externalPatternInputSink) {
        final ResourceList iterationInputsSimulated = calculateIterationInputs(Action.SIMULATE);
        if (!extractAll(iterationInputsSimulated, internalStorage, Action.SIMULATE)) {
            return false;
        }
        LOGGER.debug("Stepping {}", pattern);
        final ResourceList iterationInputs = calculateIterationInputs(Action.EXECUTE);
        extractAll(iterationInputs, internalStorage, Action.EXECUTE);
        pattern.outputs().forEach(output -> returnOutput(internalStorage, rootStorage, output));
        return useIteration();
    }

    private void returnOutput(final MutableResourceList internalStorage,
                              final RootStorage rootStorage,
                              final ResourceAmount output) {
        if (root) {
            LOGGER.debug("Inserting {}x {} into root storage", output.amount(), output.resource());
            final long inserted = rootStorage.insert(output.resource(), output.amount(), Action.EXECUTE, Actor.EMPTY);
            if (inserted != output.amount()) {
                final long remainder = output.amount() - inserted;
                LOGGER.debug("Inserting overflow {}x {} into internal storage", remainder, output.resource());
                internalStorage.add(output.resource(), remainder);
            }
        } else {
            LOGGER.debug("Inserting {}x {} into internal storage", output.amount(), output.resource());
            internalStorage.add(output);
        }
    }

    @Override
    long interceptInsertion(final ResourceKey resource, final long amount) {
        return 0;
    }

    protected boolean useIteration() {
        iterationsRemaining--;
        LOGGER.debug("Stepped {} with {} iterations remaining", pattern, iterationsRemaining);
        return iterationsRemaining == 0;
    }
}
