package com.refinedmods.refinedstorage.common.api.support.resource;

import java.util.List;

import net.minecraft.tags.TagKey;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.6")
public record ResourceTag(TagKey<?> key, List<PlatformResourceKey> resources) {
}
