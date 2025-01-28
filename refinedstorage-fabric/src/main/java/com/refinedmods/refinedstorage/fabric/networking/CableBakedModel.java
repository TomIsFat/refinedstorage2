package com.refinedmods.refinedstorage.fabric.networking;

import com.refinedmods.refinedstorage.common.networking.CableConnections;
import com.refinedmods.refinedstorage.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.fabric.support.render.QuadRotators;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class CableBakedModel extends ForwardingBakedModel {
    private final QuadRotators quadRotators;
    private final BakedModel extension;

    CableBakedModel(final QuadRotators quadRotators, final BakedModel core, final BakedModel extension) {
        this.wrapped = core;
        this.quadRotators = quadRotators;
        this.extension = extension;
    }

    @Override
    public void emitBlockQuads(final BlockAndTintGetter blockView,
                               final BlockState state,
                               final BlockPos pos,
                               final Supplier<RandomSource> randomSupplier,
                               final RenderContext context) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        final Object data = blockView.getBlockEntityRenderData(pos);
        if (!(data instanceof CableConnections(boolean n, boolean e, boolean s, boolean w, boolean up, boolean down))) {
            return;
        }
        if (n) {
            addExtension(blockView, state, pos, Direction.NORTH, randomSupplier, context);
        }
        if (e) {
            addExtension(blockView, state, pos, Direction.EAST, randomSupplier, context);
        }
        if (s) {
            addExtension(blockView, state, pos, Direction.SOUTH, randomSupplier, context);
        }
        if (w) {
            addExtension(blockView, state, pos, Direction.WEST, randomSupplier, context);
        }
        if (up) {
            addExtension(blockView, state, pos, Direction.UP, randomSupplier, context);
        }
        if (down) {
            addExtension(blockView, state, pos, Direction.DOWN, randomSupplier, context);
        }
    }

    private void addExtension(final BlockAndTintGetter blockView,
                              final BlockState state,
                              final BlockPos pos,
                              final Direction direction,
                              final Supplier<RandomSource> randomSupplier,
                              final RenderContext context) {
        context.pushTransform(quadRotators.forDirection(BiDirection.forDirection(direction)));
        extension.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }
}
