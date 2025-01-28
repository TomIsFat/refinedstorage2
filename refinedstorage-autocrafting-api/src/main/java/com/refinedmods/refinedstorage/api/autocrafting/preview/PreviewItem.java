package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.9")
public record PreviewItem(ResourceKey resource, long available, long missing, long toCraft) {
}
