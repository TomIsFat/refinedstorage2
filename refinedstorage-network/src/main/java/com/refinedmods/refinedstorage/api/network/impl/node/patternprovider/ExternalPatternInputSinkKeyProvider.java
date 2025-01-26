package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternInputSinkKey;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ExternalPatternInputSinkKeyProvider {
    @Nullable
    ExternalPatternInputSinkKey getKey();
}
