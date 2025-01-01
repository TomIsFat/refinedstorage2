package com.refinedmods.refinedstorage.network.test.fixtures;

import com.refinedmods.refinedstorage.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.NetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.autocrafting.AutocraftingNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.GraphNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.impl.security.SecurityNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.impl.storage.StorageNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;

import java.util.concurrent.Executors;

public final class NetworkTestFixtures {
    public static final ComponentMapFactory<NetworkComponent, Network> NETWORK_COMPONENT_MAP_FACTORY =
        new ComponentMapFactory<>();

    static {
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            EnergyNetworkComponent.class,
            network -> new EnergyNetworkComponentImpl()
        );
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            GraphNetworkComponent.class,
            GraphNetworkComponentImpl::new
        );
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            StorageNetworkComponent.class,
            network -> new StorageNetworkComponentImpl(MutableResourceListImpl.orderPreserving())
        );
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            SecurityNetworkComponent.class,
            network -> new SecurityNetworkComponentImpl(SecurityPolicy.of(PermissionFixtures.ALLOW_BY_DEFAULT))
        );
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            AutocraftingNetworkComponent.class,
            network -> new AutocraftingNetworkComponentImpl(
                () -> {
                    throw new UnsupportedOperationException("Storage not accessible from here (yet)");
                },
                new FakeTaskStatusProvider(),
                Executors.newSingleThreadExecutor()
            )
        );
    }

    private NetworkTestFixtures() {
    }
}
