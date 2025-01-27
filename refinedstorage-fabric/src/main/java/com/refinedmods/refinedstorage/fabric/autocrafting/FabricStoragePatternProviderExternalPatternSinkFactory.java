package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternSinkFactory;
import com.refinedmods.refinedstorage.fabric.api.FabricStorageExternalPatternSinkStrategyFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStoragePatternProviderExternalPatternSinkFactory
    implements PatternProviderExternalPatternSinkFactory {
    private final Set<FabricStorageExternalPatternSinkStrategyFactory> factories = new HashSet<>();

    public void addFactory(final FabricStorageExternalPatternSinkStrategyFactory factory) {
        factories.add(factory);
    }

    @Override
    public PatternProviderExternalPatternSink create(final ServerLevel level,
                                                     final BlockPos pos,
                                                     final Direction direction) {
        return new FabricStoragePatternProviderExternalPatternSink(factories
            .stream()
            .map(factory -> factory.create(level, pos, direction))
            .collect(Collectors.toSet()));
    }
}
