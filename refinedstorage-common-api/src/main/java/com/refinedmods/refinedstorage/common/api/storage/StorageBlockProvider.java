package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.11")
public interface StorageBlockProvider {
    SerializableStorage createStorage(Runnable listener);

    Component getDisplayName();

    long getEnergyUsage();

    ResourceFactory getResourceFactory();

    BlockEntityType<?> getBlockEntityType();

    MenuType<?> getMenuType();
}
