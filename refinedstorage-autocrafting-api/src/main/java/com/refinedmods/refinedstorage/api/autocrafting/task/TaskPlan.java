package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Map;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public record TaskPlan(Pattern rootPattern,
                       Map<Pattern, PatternPlan> patterns,
                       Collection<ResourceAmount> initialRequirements) {
    public PatternPlan getPattern(final Pattern pattern) {
        return patterns.get(pattern);
    }

    public record PatternPlan(boolean root, long iterations, Map<Integer, Map<ResourceKey, Long>> ingredients) {
    }
}
