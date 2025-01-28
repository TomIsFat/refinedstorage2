package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;

import java.util.Collection;
import java.util.Set;

class CompositePatternProviderExternalPatternSink
    implements PlatformPatternProviderExternalPatternSink {
    private final Set<PlatformPatternProviderExternalPatternSink> sinks;

    CompositePatternProviderExternalPatternSink(final Set<PlatformPatternProviderExternalPatternSink> sinks) {
        this.sinks = sinks;
    }

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        ExternalPatternSink.Result result = ExternalPatternSink.Result.SKIPPED;
        for (final PatternProviderExternalPatternSink sink : sinks) {
            final ExternalPatternSink.Result sinkResult = sink.accept(resources, action);
            if (sinkResult == ExternalPatternSink.Result.REJECTED) {
                return sinkResult;
            }
            result = and(result, sinkResult);
        }
        return result;
    }

    private ExternalPatternSink.Result and(final ExternalPatternSink.Result a,
                                           final ExternalPatternSink.Result b) {
        if (a == ExternalPatternSink.Result.SKIPPED) {
            return b;
        } else if (a == ExternalPatternSink.Result.REJECTED || b == ExternalPatternSink.Result.REJECTED) {
            return ExternalPatternSink.Result.REJECTED;
        } else {
            return ExternalPatternSink.Result.ACCEPTED;
        }
    }

    @Override
    public boolean isEmpty() {
        for (final PlatformPatternProviderExternalPatternSink sink : sinks) {
            if (!sink.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
