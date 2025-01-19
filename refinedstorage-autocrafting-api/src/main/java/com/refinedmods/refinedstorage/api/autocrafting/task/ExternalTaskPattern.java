package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExternalTaskPattern extends AbstractTaskPattern {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalTaskPattern.class);

    private final MutableResourceList expectedOutputs = MutableResourceListImpl.create();
    private final ResourceList simulatedIterationInputs;
    private final long originalIterationsToSendToSink;
    private long iterationsToSendToSink;
    private long iterationsReceived;

    ExternalTaskPattern(final Pattern pattern, final TaskPlan.PatternPlan plan) {
        super(pattern, plan);
        this.originalIterationsToSendToSink = plan.iterations();
        pattern.outputs().forEach(
            output -> expectedOutputs.add(output.resource(), output.amount() * plan.iterations())
        );
        this.iterationsToSendToSink = plan.iterations();
        this.simulatedIterationInputs = calculateIterationInputs(Action.SIMULATE);
    }

    @Override
    boolean step(final MutableResourceList internalStorage,
                 final RootStorage rootStorage,
                 final ExternalPatternInputSink externalPatternInputSink) {
        if (expectedOutputs.isEmpty()) {
            return true;
        }
        if (iterationsToSendToSink == 0) {
            return false;
        }
        if (!acceptsIterationInputs(internalStorage, externalPatternInputSink)) {
            return false;
        }
        LOGGER.debug("Stepped {} with {} iterations remaining", pattern, iterationsToSendToSink);
        iterationsToSendToSink--;
        return false;
    }

    @Override
    RootStorageListener.InterceptResult interceptInsertion(final ResourceKey resource, final long amount) {
        final long needed = expectedOutputs.get(resource);
        if (needed == 0) {
            return RootStorageListener.InterceptResult.EMPTY;
        }
        final long reserved = Math.min(needed, amount);
        expectedOutputs.remove(resource, reserved);
        updateIterationsReceived();
        final long intercepted = root ? 0 : reserved;
        return new RootStorageListener.InterceptResult(reserved, intercepted);
    }

    private void updateIterationsReceived() {
        long result = originalIterationsToSendToSink;
        for (final ResourceAmount output : pattern.outputs()) {
            final long expected = output.amount() * originalIterationsToSendToSink;
            final long stillNeeded = expectedOutputs.get(output.resource());
            final long receivedOutputs = expected - stillNeeded;
            final long receivedOutputIterations = receivedOutputs / output.amount();
            if (result > receivedOutputIterations) {
                result = receivedOutputIterations;
            }
        }
        this.iterationsReceived = result;
    }

    @Override
    void appendStatus(final TaskStatusBuilder builder) {
        if (iterationsToSendToSink > 0) {
            for (final ResourceAmount output : pattern.outputs()) {
                builder.scheduled(output.resource(), output.amount() * iterationsToSendToSink);
            }
        }
        final long iterationsSentToSink = originalIterationsToSendToSink - iterationsToSendToSink;
        final long iterationsProcessing = iterationsSentToSink - iterationsReceived;
        if (iterationsProcessing > 0) {
            for (final ResourceKey input : simulatedIterationInputs.getAll()) {
                builder.processing(input, simulatedIterationInputs.get(input) * iterationsProcessing);
            }
        }
    }

    @Override
    long getWeight() {
        return iterationsToSendToSink;
    }

    @Override
    double getPercentageCompleted() {
        return iterationsReceived / (double) originalIterationsToSendToSink;
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
        // If the sink does not accept the inputs
        // we cannot return the extracted resources to the internal storage
        // because we have already deducted from the iteration inputs
        // and because the sink might have still accepted some resources halfway.
        // If we returned the extracted resources to the internal storage and correct the
        // iteration inputs, it would potentially duplicate the resources
        // across the sink and the internal storage.
        // The end result is that we lie, do as if the insertion was successful,
        // and potentially void the extracted resources from the internal storage.
        if (!externalPatternInputSink.accept(pattern, iterationInputs.copyState(), Action.EXECUTE)) {
            LOGGER.warn("External sink {} did not accept all inputs for pattern {}", externalPatternInputSink, pattern);
        }
        return true;
    }
}
