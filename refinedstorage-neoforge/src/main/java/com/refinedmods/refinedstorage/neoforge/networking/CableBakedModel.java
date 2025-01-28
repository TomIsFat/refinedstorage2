package com.refinedmods.refinedstorage.neoforge.networking;

import com.refinedmods.refinedstorage.common.networking.CableConnections;
import com.refinedmods.refinedstorage.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.neoforge.support.render.ModelProperties;
import com.refinedmods.refinedstorage.neoforge.support.render.RotationTranslationModelBaker;
import com.refinedmods.refinedstorage.neoforge.support.render.TransformationBuilder;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

class CableBakedModel extends BakedModelWrapper<BakedModel> {
    private final LoadingCache<CacheKey, List<BakedQuad>> cache;
    private final RotationTranslationModelBaker extensionBaker;

    CableBakedModel(final BakedModel core, final RotationTranslationModelBaker extensionBaker) {
        super(core);
        this.extensionBaker = extensionBaker;
        this.cache = CacheBuilder.newBuilder().build(CacheLoader.from(cacheKey -> {
            final RandomSource rand = RandomSource.create();
            final List<BakedQuad> quads = new ArrayList<>(super.getQuads(
                null,
                cacheKey.side,
                rand,
                ModelData.EMPTY,
                null
            ));
            if (cacheKey.connections.north()) {
                addExtension(quads, Direction.NORTH, cacheKey.side, rand);
            }
            if (cacheKey.connections.east()) {
                addExtension(quads, Direction.EAST, cacheKey.side, rand);
            }
            if (cacheKey.connections.south()) {
                addExtension(quads, Direction.SOUTH, cacheKey.side, rand);
            }
            if (cacheKey.connections.west()) {
                addExtension(quads, Direction.WEST, cacheKey.side, rand);
            }
            if (cacheKey.connections.up()) {
                addExtension(quads, Direction.UP, cacheKey.side, rand);
            }
            if (cacheKey.connections.down()) {
                addExtension(quads, Direction.DOWN, cacheKey.side, rand);
            }
            return quads;
        }));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state,
                                    @Nullable final Direction side,
                                    final RandomSource rand,
                                    final ModelData extraData,
                                    @Nullable final RenderType renderType) {
        final CableConnections connections = extraData.get(ModelProperties.CABLE_CONNECTIONS);
        if (connections == null) {
            return super.getQuads(state, side, rand, extraData, renderType);
        }
        return cache.getUnchecked(new CacheKey(side, connections));
    }

    private void addExtension(final List<BakedQuad> quads,
                              final Direction direction,
                              @Nullable final Direction side,
                              final RandomSource randomSource) {
        final Transformation transformation = TransformationBuilder.create().rotate(BiDirection.forDirection(direction))
            .build();
        quads.addAll(extensionBaker.bake(transformation, side, randomSource));
    }

    private record CacheKey(@Nullable Direction side, CableConnections connections) {
    }
}
