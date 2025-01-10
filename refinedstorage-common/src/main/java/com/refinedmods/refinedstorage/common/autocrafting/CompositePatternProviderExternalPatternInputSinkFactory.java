package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternInputSink;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternInputSinkFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class CompositePatternProviderExternalPatternInputSinkFactory
    implements PatternProviderExternalPatternInputSinkFactory {
    private final Set<PatternProviderExternalPatternInputSinkFactory> factories = new HashSet<>();

    public void addFactory(final PatternProviderExternalPatternInputSinkFactory factory) {
        factories.add(factory);
    }

    @Override
    public PatternProviderExternalPatternInputSink create(final ServerLevel level,
                                                          final BlockPos pos,
                                                          final Direction direction) {
        return new CompositePatternProviderExternalPatternInputSink(factories.stream()
            .map(factory -> factory.create(level, pos, direction))
            .collect(Collectors.toSet()));
    }
}
