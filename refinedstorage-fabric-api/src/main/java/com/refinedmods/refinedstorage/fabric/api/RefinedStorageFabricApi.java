package com.refinedmods.refinedstorage.fabric.api;

import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.5")
public interface RefinedStorageFabricApi {
    RefinedStorageFabricApi INSTANCE = new RefinedStorageFabricApiProxy();

    BlockApiLookup<NetworkNodeContainerProvider, @NullableType Direction> getNetworkNodeContainerProviderLookup();

    void addStorageExternalPatternSinkStrategyFactory(FabricStorageExternalPatternSinkStrategyFactory factory);
}
