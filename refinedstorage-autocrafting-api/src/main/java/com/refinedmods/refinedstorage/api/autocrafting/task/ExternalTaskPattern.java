package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExternalTaskPattern extends AbstractTaskPattern {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalTaskPattern.class);

    private final MutableResourceList expectedOutputs = MutableResourceListImpl.create();
    private long iterationsToSendToSink;

    ExternalTaskPattern(final Pattern pattern, final TaskPlan.PatternPlan plan) {
        super(pattern, plan);
        this.iterationsToSendToSink = plan.iterations();
        pattern.outputs().forEach(
            output -> expectedOutputs.add(output.resource(), output.amount() * plan.iterations())
        );
    }

    @Override
    boolean step(final MutableResourceList internalStorage, final ExternalPatternInputSink externalPatternInputSink) {

        if (iterationsToSendToSink == 0) {
            return false;
        }
        if (!acceptsIterationInputs(internalStorage, externalPatternInputSink)) {
            return false;
        }
        LOGGER.info("Stepped {} with {} iterations remaining", pattern, iterationsToSendToSink);
        iterationsToSendToSink--;
        return false;
    }

    @Override
    long interceptInsertion(final ResourceKey resource, final long amount) {
        final long needed = expectedOutputs.get(resource);
        if (needed > 0) {
            final long available = Math.min(needed, amount);
            expectedOutputs.remove(resource, available);
            return available;
        }
        return 0;
    }

    private boolean acceptsIterationInputs(final MutableResourceList internalStorage,
                                           final ExternalPatternInputSink externalPatternInputSink) {
        final ResourceList iterationInputsSimulated = calculateIterationInputs(Action.SIMULATE);
        if (!extractAll(iterationInputsSimulated, internalStorage, Action.SIMULATE)) {
            return false;
        }
        if (!externalPatternInputSink.accept(pattern, iterationInputsSimulated.copyState(), Action.SIMULATE)) {
            return false;
        }
        final ResourceList iterationInputs = calculateIterationInputs(Action.EXECUTE);
        extractAll(iterationInputs, internalStorage, Action.EXECUTE);
        if (!externalPatternInputSink.accept(pattern, iterationInputs.copyState(), Action.EXECUTE)) {
            // TODO: return here.
            return false;
        }
        return true;
    }
}
