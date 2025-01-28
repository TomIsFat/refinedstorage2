package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskStateChangeListener implements StateTrackedStorage.Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiskStateChangeListener.class);

    private final BlockEntity blockEntity;
    private final RateLimiter rateLimiter = RateLimiter.create(1);

    private boolean syncRequested;

    public DiskStateChangeListener(final BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void onStorageStateChanged() {
        syncRequested = true;
    }

    public void updateIfNecessary() {
        if (!syncRequested) {
            return;
        }
        if (!rateLimiter.tryAcquire()) {
            return;
        }
        LOGGER.debug("Disk state change for block at {}", blockEntity.getBlockPos());
        syncRequested = false;
        PlatformUtil.sendBlockUpdateToClient(blockEntity.getLevel(), blockEntity.getBlockPos());
    }
}
