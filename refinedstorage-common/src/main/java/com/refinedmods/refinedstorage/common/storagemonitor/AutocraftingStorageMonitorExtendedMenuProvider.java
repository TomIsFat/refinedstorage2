package com.refinedmods.refinedstorage.common.storagemonitor;

import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

class AutocraftingStorageMonitorExtendedMenuProvider implements ExtendedMenuProvider<PlatformResourceKey> {
    private final PlatformResourceKey resource;
    private final StorageMonitorBlockEntity blockEntity;

    AutocraftingStorageMonitorExtendedMenuProvider(final PlatformResourceKey resource,
                                                   final StorageMonitorBlockEntity blockEntity) {
        this.resource = resource;
        this.blockEntity = blockEntity;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId,
                                            final Inventory inventory,
                                            final Player player) {
        return new AutocraftingStorageMonitorContainerMenu(syncId, resource, blockEntity);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.STORAGE_MONITOR;
    }

    @Override
    public PlatformResourceKey getMenuData() {
        return resource;
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, PlatformResourceKey> getMenuCodec() {
        return ResourceCodecs.STREAM_CODEC;
    }
}
