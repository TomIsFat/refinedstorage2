package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class MutableTaskPlan {
    @Nullable
    private final Pattern pattern;
    private final Map<Pattern, MutablePatternPlan> patterns;
    private final MutableResourceList initialRequirements;
    private boolean missing;

    MutableTaskPlan() {
        this(null, new LinkedHashMap<>(), MutableResourceListImpl.create(), false);
    }

    private MutableTaskPlan(@Nullable final Pattern pattern,
                            final Map<Pattern, MutablePatternPlan> patterns,
                            final MutableResourceList initialRequirements,
                            final boolean missing) {
        this.pattern = pattern;
        this.patterns = patterns;
        this.initialRequirements = initialRequirements;
        this.missing = missing;
    }

    void addOrUpdatePattern(final Pattern usedPattern, final long iterations) {
        patterns.computeIfAbsent(usedPattern, MutablePatternPlan::new).addIterations(iterations);
    }

    void addToExtract(final ResourceKey resource, final long amount) {
        initialRequirements.add(resource, amount);
    }

    void addUsedIngredient(final Pattern ingredientPattern,
                           final int ingredientIndex,
                           final ResourceKey resource,
                           final long amount) {
        final MutablePatternPlan patternPlan = requireNonNull(patterns.get(ingredientPattern));
        patternPlan.addUsedIngredient(ingredientIndex, resource, amount);
    }

    MutableTaskPlan copy(final Pattern childPattern) {
        final Map<Pattern, MutablePatternPlan> patternsCopy = new LinkedHashMap<>();
        for (final Map.Entry<Pattern, MutablePatternPlan> entry : patterns.entrySet()) {
            patternsCopy.put(entry.getKey(), entry.getValue().copy());
        }
        return new MutableTaskPlan(
            pattern == null ? childPattern : pattern,
            patternsCopy,
            initialRequirements.copy(),
            missing
        );
    }

    Optional<TaskPlan> getPlan() {
        if (missing || pattern == null) {
            return Optional.empty();
        }
        final Map<Pattern, TaskPlan.PatternPlan> finalPatterns = Collections.unmodifiableMap(patterns.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getPlan(),
                (a, b) -> a,
                LinkedHashMap::new
            )));
        return Optional.of(new TaskPlan(pattern, finalPatterns, initialRequirements.copyState()));
    }

    void setMissing() {
        this.missing = true;
    }
}
