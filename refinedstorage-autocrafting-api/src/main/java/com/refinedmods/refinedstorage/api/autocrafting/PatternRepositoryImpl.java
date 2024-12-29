package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class PatternRepositoryImpl implements PatternRepository {
    private final Set<Pattern> patterns = new HashSet<>();
    private final Map<ResourceKey, PriorityQueue<PatternHolder>> patternsByOutput = new HashMap<>();
    private final Set<Pattern> patternsView = Collections.unmodifiableSet(patterns);
    private final Set<ResourceKey> outputs = new HashSet<>();

    @Override
    public void add(final Pattern pattern, final int priority) {
        patterns.add(pattern);
        outputs.addAll(pattern.getOutputResources());
        for (final ResourceKey output : pattern.getOutputResources()) {
            patternsByOutput.computeIfAbsent(output, k -> new PriorityQueue<>(
                Comparator.comparingInt(PatternHolder::priority).reversed()
            )).add(new PatternHolder(pattern, priority));
        }
    }

    @Override
    public void update(final Pattern pattern, final int priority) {
        for (final ResourceKey output : pattern.getOutputResources()) {
            final PriorityQueue<PatternHolder> holders = patternsByOutput.get(output);
            if (holders == null) {
                continue;
            }
            holders.removeIf(holder -> holder.pattern.equals(pattern));
            holders.add(new PatternHolder(pattern, priority));
        }
    }

    @Override
    public void remove(final Pattern pattern) {
        patterns.remove(pattern);
        for (final ResourceKey output : pattern.getOutputResources()) {
            final PriorityQueue<PatternHolder> holders = patternsByOutput.get(output);
            if (holders == null) {
                continue;
            }
            holders.removeIf(holder -> holder.pattern.equals(pattern));
            if (holders.isEmpty()) {
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
        final PriorityQueue<PatternHolder> holders = patternsByOutput.get(output);
        if (holders == null) {
            return Collections.emptyList();
        }
        return holders.stream().map(holder -> holder.pattern).toList();
    }

    @Override
    public Set<ResourceKey> getOutputs() {
        return outputs;
    }

    @Override
    public Set<Pattern> getAll() {
        return patternsView;
    }

    private record PatternHolder(Pattern pattern, int priority) {
    }
}
