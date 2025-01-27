package com.refinedmods.refinedstorage.common.api.autocrafting;

import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface PlatformPatternProviderExternalPatternSink extends PatternProviderExternalPatternSink {
    boolean isEmpty();
}
