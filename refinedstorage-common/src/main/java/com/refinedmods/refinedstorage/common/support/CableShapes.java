package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.networking.CableConnections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.Block.box;

public final class CableShapes {
    private static final VoxelShape CORE = box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape NORTH = box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape EAST = box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SOUTH = box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape WEST = box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape UP = box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape DOWN = box(6, 0, 6, 10, 6, 10);
    private static final Map<CableConnections, VoxelShape> CACHE = new ConcurrentHashMap<>();

    private CableShapes() {
    }

    public static VoxelShape getShape(final CableConnections connections) {
        return CACHE.computeIfAbsent(connections, CableShapes::computeShape);
    }

    private static VoxelShape computeShape(final CableConnections connections) {
        VoxelShape shape = CORE;
        if (connections.north()) {
            shape = Shapes.or(shape, NORTH);
        }
        if (connections.east()) {
            shape = Shapes.or(shape, EAST);
        }
        if (connections.south()) {
            shape = Shapes.or(shape, SOUTH);
        }
        if (connections.west()) {
            shape = Shapes.or(shape, WEST);
        }
        if (connections.up()) {
            shape = Shapes.or(shape, UP);
        }
        if (connections.down()) {
            shape = Shapes.or(shape, DOWN);
        }
        return shape;
    }
}
