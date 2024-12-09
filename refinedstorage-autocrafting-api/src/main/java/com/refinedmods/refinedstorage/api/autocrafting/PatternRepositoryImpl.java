package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatternRepositoryImpl implements PatternRepository {
    private final Set<Pattern> patterns = new HashSet<>();
    private final Map<ResourceKey, List<Pattern>> patternsByOutput = new HashMap<>();
    private final Set<Pattern> patternsView = Collections.unmodifiableSet(patterns);
    private final Set<ResourceKey> outputs = new HashSet<>();

    @Override
    public void add(final Pattern pattern) {
        patterns.add(pattern);
        outputs.addAll(pattern.getOutputResources());
        for (final ResourceKey output : pattern.getOutputResources()) {
            patternsByOutput.computeIfAbsent(output, k -> new ArrayList<>()).add(pattern);
        }
    }

    @Override
    public void remove(final Pattern pattern) {
        patterns.remove(pattern);
        for (final ResourceKey output : pattern.getOutputResources()) {
            final List<Pattern> byOutput = patternsByOutput.get(output);
            if (byOutput == null) {
                continue;
            }
            byOutput.remove(pattern);
            if (byOutput.isEmpty()) {
                patternsByOutput.remove(output);
            }
            final boolean noOtherPatternHasThisOutput = patterns.stream()
                .noneMatch(otherPattern -> otherPattern.getOutputResources().contains(output));
            if (noOtherPatternHasThisOutput) {
                outputs.remove(output);
            }
        }
    }

    @Override
    public List<Pattern> getByOutput(final ResourceKey output) {
        return patternsByOutput.getOrDefault(output, Collections.emptyList());
    }

    @Override
    public Set<ResourceKey> getOutputs() {
        return outputs;
    }

    @Override
    public Set<Pattern> getAll() {
        return patternsView;
    }
}
