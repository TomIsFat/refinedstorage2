package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;

class ExternalTaskPattern extends AbstractTaskPattern {
    private long iterationsToSendToSink;

    ExternalTaskPattern(final Pattern pattern, final TaskPlan.PatternPlan plan) {
        super(pattern, plan);
        this.iterationsToSendToSink = plan.iterations();
    }

    @Override
    boolean step(final MutableResourceList internalStorage, final ExternalPatternInputSink externalPatternInputSink) {
        if (iterationsToSendToSink == 0) {
            return false;
        }
        if (!acceptsIterationInputs(internalStorage, externalPatternInputSink)) {
            return false;
        }
        iterationsToSendToSink--;
        return false;
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
