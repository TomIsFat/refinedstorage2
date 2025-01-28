package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import net.minecraft.world.entity.player.Player;

public interface ExtractTransaction extends AutoCloseable {
    ExtractTransaction NOOP = new ExtractTransaction() {
        @Override
        public boolean extract(final ItemResource resource, final Player player) {
            return false;
        }

        @Override
        public void close() {
            // no op
        }
    };

    boolean extract(ItemResource resource, Player player);

    @Override
    void close();
}
