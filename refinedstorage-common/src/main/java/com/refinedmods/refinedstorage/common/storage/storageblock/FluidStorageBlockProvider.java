package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockProvider;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.StorageTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class FluidStorageBlockProvider implements StorageBlockProvider {
    private final FluidStorageVariant variant;
    private final Component displayName;

    public FluidStorageBlockProvider(final FluidStorageVariant variant) {
        this.variant = variant;
        this.displayName = createTranslation("block", String.format("%s_fluid_storage_block", variant.getName()));
    }

    @Override
    public SerializableStorage createStorage(final Runnable listener) {
        return StorageTypes.FLUID.create(variant.getCapacity(), listener);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public long getEnergyUsage() {
        return switch (variant) {
            case SIXTY_FOUR_B -> Platform.INSTANCE.getConfig().getFluidStorageBlock().get64bEnergyUsage();
            case TWO_HUNDRED_FIFTY_SIX_B -> Platform.INSTANCE.getConfig().getFluidStorageBlock().get256bEnergyUsage();
            case THOUSAND_TWENTY_FOUR_B -> Platform.INSTANCE.getConfig().getFluidStorageBlock().get1024bEnergyUsage();
            case FOUR_THOUSAND_NINETY_SIX_B ->
                Platform.INSTANCE.getConfig().getFluidStorageBlock().get4096bEnergyUsage();
            case CREATIVE -> Platform.INSTANCE.getConfig().getFluidStorageBlock().getCreativeEnergyUsage();
        };
    }

    @Override
    public ResourceFactory getResourceFactory() {
        return RefinedStorageApi.INSTANCE.getFluidResourceFactory();
    }

    @Override
    public BlockEntityType<?> getBlockEntityType() {
        return BlockEntities.INSTANCE.getFluidStorageBlock(variant);
    }

    @Override
    public MenuType<?> getMenuType() {
        return Menus.INSTANCE.getFluidStorage();
    }
}
