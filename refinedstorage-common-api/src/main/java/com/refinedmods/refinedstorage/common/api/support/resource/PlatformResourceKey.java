package com.refinedmods.refinedstorage.common.api.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.4")
public interface PlatformResourceKey extends ResourceKey {
    long getInterfaceExportLimit();

    long getProcessingPatternLimit();

    List<ResourceTag> getTags();

    ResourceType getResourceType();
}
