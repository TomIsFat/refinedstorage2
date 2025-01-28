package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import net.minecraft.world.entity.player.Player;

public class DirectCommitExtractTransaction implements ExtractTransaction {
    private final RootStorage rootStorage;

    public DirectCommitExtractTransaction(final RootStorage rootStorage) {
        this.rootStorage = rootStorage;
    }

    @Override
    public boolean extract(final ItemResource resource, final Player player) {
        return rootStorage.extract(resource, 1, Action.EXECUTE, new PlayerActor(player)) == 1;
    }

    @Override
    public void close() {
        // no op
    }
}
