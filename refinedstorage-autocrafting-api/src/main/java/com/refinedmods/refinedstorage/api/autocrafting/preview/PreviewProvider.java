package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.9")
public interface PreviewProvider {
    Optional<Preview> getPreview(ResourceKey resource, long amount);

    long getMaxAmount(ResourceKey resource);

    boolean startTask(ResourceKey resource, long amount);
}
