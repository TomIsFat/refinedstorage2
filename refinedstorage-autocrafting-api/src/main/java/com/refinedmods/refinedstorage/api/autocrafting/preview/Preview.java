package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.9")
public record Preview(PreviewType type, List<PreviewItem> items, List<ResourceAmount> outputsOfPatternWithCycle) {
}
