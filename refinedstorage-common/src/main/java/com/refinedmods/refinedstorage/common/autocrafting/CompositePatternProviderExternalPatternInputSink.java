package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternInputSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternInputSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.Set;

class CompositePatternProviderExternalPatternInputSink
    implements PatternProviderExternalPatternInputSink {
    private final Set<PatternProviderExternalPatternInputSink> sinks;

    CompositePatternProviderExternalPatternInputSink(final Set<PatternProviderExternalPatternInputSink> sinks) {
        this.sinks = sinks;
    }

    @Override
    public ExternalPatternInputSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        ExternalPatternInputSink.Result result = ExternalPatternInputSink.Result.SKIPPED;
        for (final PatternProviderExternalPatternInputSink sink : sinks) {
            final ExternalPatternInputSink.Result sinkResult = sink.accept(resources, action);
            if (sinkResult == ExternalPatternInputSink.Result.REJECTED) {
                return sinkResult;
            }
            result = result.and(sinkResult);
        }
        return result;
    }
}
