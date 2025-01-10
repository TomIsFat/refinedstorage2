package com.refinedmods.refinedstorage.common.autocrafting;

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
    public boolean accept(final Collection<ResourceAmount> resources, final Action action) {
        for (final PatternProviderExternalPatternInputSink sink : sinks) {
            if (!sink.accept(resources, action)) {
                return false;
            }
        }
        return true;
    }
}
