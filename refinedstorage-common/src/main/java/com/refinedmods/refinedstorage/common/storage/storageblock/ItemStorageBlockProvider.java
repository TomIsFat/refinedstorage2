package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockProvider;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.storage.StorageTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ItemStorageBlockProvider implements StorageBlockProvider {
    private final ItemStorageVariant variant;
    private final Component displayName;

    public ItemStorageBlockProvider(final ItemStorageVariant variant) {
        this.variant = variant;
        this.displayName = createTranslation("block", String.format("%s_storage_block", variant.getName()));
    }

    @Override
    public SerializableStorage createStorage(final Runnable listener) {
        return StorageTypes.ITEM.create(variant.getCapacity(), listener);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public long getEnergyUsage() {
        return switch (variant) {
            case ONE_K -> Platform.INSTANCE.getConfig().getStorageBlock().get1kEnergyUsage();
            case FOUR_K -> Platform.INSTANCE.getConfig().getStorageBlock().get4kEnergyUsage();
            case SIXTEEN_K -> Platform.INSTANCE.getConfig().getStorageBlock().get16kEnergyUsage();
            case SIXTY_FOUR_K -> Platform.INSTANCE.getConfig().getStorageBlock().get64kEnergyUsage();
            case CREATIVE -> Platform.INSTANCE.getConfig().getStorageBlock().getCreativeEnergyUsage();
        };
    }

    @Override
    public ResourceFactory getResourceFactory() {
        return RefinedStorageApi.INSTANCE.getItemResourceFactory();
    }

    @Override
    public BlockEntityType<?> getBlockEntityType() {
        return BlockEntities.INSTANCE.getItemStorageBlock(variant);
    }

    @Override
    public MenuType<?> getMenuType() {
        return Menus.INSTANCE.getItemStorage();
    }
}
