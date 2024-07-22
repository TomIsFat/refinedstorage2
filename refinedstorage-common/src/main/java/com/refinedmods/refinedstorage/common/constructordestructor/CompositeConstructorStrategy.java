package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.constructordestructor.ConstructorStrategy;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.entity.player.Player;

class CompositeConstructorStrategy implements ConstructorStrategy {
    private final List<ConstructorStrategy> strategies;

    CompositeConstructorStrategy(final List<ConstructorStrategy> strategies) {
        this.strategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean apply(final ResourceKey resource,
                         final Actor actor,
                         final Player player,
                         final Network network) {
        for (final ConstructorStrategy strategy : strategies) {
            if (strategy.apply(resource, actor, player, network)) {
                return true;
            }
        }
        return false;
    }
}
