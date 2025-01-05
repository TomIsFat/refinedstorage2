package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternInputSink;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternInputSinkFactory;
import com.refinedmods.refinedstorage.fabric.api.FabricStorageExternalPatternInputSinkStrategyFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStoragePatternProviderExternalPatternInputSinkFactory implements
    PatternProviderExternalPatternInputSinkFactory {
    private final Set<FabricStorageExternalPatternInputSinkStrategyFactory> factories = new HashSet<>();

    public void addFactory(final FabricStorageExternalPatternInputSinkStrategyFactory factory) {
        factories.add(factory);
    }

    @Override
    public PatternProviderExternalPatternInputSink create(final ServerLevel level,
                                                          final BlockPos pos,
                                                          final Direction direction) {
        return new FabricStoragePatternProviderExternalPatternInputSink(factories
            .stream()
            .map(factory -> factory.create(level, pos, direction))
            .collect(Collectors.toSet()));
    }
}
