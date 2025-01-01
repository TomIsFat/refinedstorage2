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

import static java.util.Objects.requireNonNull;

public class MutableTaskPlan {
    private final Map<Pattern, MutablePatternPlan> patterns;
    private final MutableResourceList initialRequirements;
    private boolean missing;

    MutableTaskPlan() {
        this(new LinkedHashMap<>(), MutableResourceListImpl.create(), false);
    }

    private MutableTaskPlan(final Map<Pattern, MutablePatternPlan> patterns,
                            final MutableResourceList initialRequirements,
                            final boolean missing) {
        this.patterns = patterns;
        this.initialRequirements = initialRequirements;
        this.missing = missing;
    }

    void addOrUpdatePattern(final Pattern pattern, final long iterations) {
        patterns.computeIfAbsent(pattern, MutablePatternPlan::new).addIterations(iterations);
    }

    void addToExtract(final ResourceKey resource, final long amount) {
        initialRequirements.add(resource, amount);
    }

    void addUsedIngredient(final Pattern pattern,
                           final int ingredientIndex,
                           final ResourceKey resource,
                           final long amount) {
        final MutablePatternPlan patternPlan = requireNonNull(patterns.get(pattern));
        patternPlan.addUsedIngredient(ingredientIndex, resource, amount);
    }

    MutableTaskPlan copy() {
        final Map<Pattern, MutablePatternPlan> patternsCopy = new LinkedHashMap<>();
        for (final Map.Entry<Pattern, MutablePatternPlan> entry : patterns.entrySet()) {
            patternsCopy.put(entry.getKey(), entry.getValue().copy());
        }
        return new MutableTaskPlan(patternsCopy, initialRequirements.copy(), missing);
    }

    Optional<TaskPlan> getPlan() {
        if (missing) {
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
        return Optional.of(new TaskPlan(finalPatterns, initialRequirements.copyState()));
    }

    void setMissing() {
        this.missing = true;
    }
}
