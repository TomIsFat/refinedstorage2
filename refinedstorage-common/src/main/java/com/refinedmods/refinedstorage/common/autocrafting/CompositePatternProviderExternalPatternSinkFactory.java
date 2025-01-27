package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternSinkFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class CompositePatternProviderExternalPatternSinkFactory
    implements PatternProviderExternalPatternSinkFactory {
    private final Set<PatternProviderExternalPatternSinkFactory> factories = new HashSet<>();

    public void addFactory(final PatternProviderExternalPatternSinkFactory factory) {
        factories.add(factory);
    }

    @Override
    public PatternProviderExternalPatternSink create(final ServerLevel level,
                                                     final BlockPos pos,
                                                     final Direction direction) {
        return new CompositePatternProviderExternalPatternSink(factories.stream()
            .map(factory -> factory.create(level, pos, direction))
            .collect(Collectors.toSet()));
    }
}
