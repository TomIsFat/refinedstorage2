package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SnapshotExtractTransaction implements ExtractTransaction {
    private final PlayerActor playerActor;
    private final RootStorage rootStorage;
    private final MutableResourceList available = MutableResourceListImpl.create();
    private final MutableResourceList used = MutableResourceListImpl.create();

    public SnapshotExtractTransaction(final Player player,
                                      final RootStorage rootStorage,
                                      final RecipeMatrixContainer craftingMatrix) {
        this.playerActor = new PlayerActor(player);
        this.rootStorage = rootStorage;
        addAvailableItems(craftingMatrix);
    }

    private void addAvailableItems(final RecipeMatrixContainer craftingMatrix) {
        for (int i = 0; i < craftingMatrix.getContainerSize(); ++i) {
            addAvailableItem(craftingMatrix, i);
        }
    }

    private void addAvailableItem(final RecipeMatrixContainer craftingMatrix, final int craftingMatrixSlotIndex) {
        final ItemStack craftingMatrixStack = craftingMatrix.getItem(craftingMatrixSlotIndex);
        if (craftingMatrixStack.isEmpty()) {
            return;
        }
        addAvailableItem(craftingMatrixStack);
    }

    private void addAvailableItem(final ItemStack craftingMatrixStack) {
        final ItemResource craftingMatrixResource = ItemResource.ofItemStack(craftingMatrixStack);
        // a single resource can occur multiple times in a recipe, only add it once
        if (!available.contains(craftingMatrixResource)) {
            final long amount = rootStorage.extract(
                craftingMatrixResource,
                Long.MAX_VALUE,
                Action.SIMULATE,
                playerActor
            );
            if (amount > 0) {
                available.add(craftingMatrixResource, amount);
            }
        }
    }

    @Override
    public boolean extract(final ItemResource resource, final Player player) {
        final boolean isAvailable = available.contains(resource);
        if (isAvailable) {
            available.remove(resource, 1);
            used.add(resource, 1);
        }
        return isAvailable;
    }

    @Override
    public void close() {
        for (final ResourceKey usedResource : used.getAll()) {
            final long amountUsed = used.get(usedResource);
            rootStorage.extract(usedResource, amountUsed, Action.EXECUTE, playerActor);
        }
    }
}
