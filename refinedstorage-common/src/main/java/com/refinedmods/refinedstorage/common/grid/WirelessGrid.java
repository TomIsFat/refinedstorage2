package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.grid.operations.NoopGridOperations;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcherManager;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.NoopStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import net.minecraft.server.level.ServerPlayer;

class WirelessGrid implements Grid {
    private final NetworkItemContext context;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();

    WirelessGrid(final NetworkItemContext context) {
        this.context = context;
    }

    private Optional<StorageNetworkComponent> getStorage() {
        return context.resolveNetwork().map(network -> network.getComponent(StorageNetworkComponent.class));
    }

    private Optional<PlatformSecurityNetworkComponent> getSecurity() {
        return context.resolveNetwork().map(network -> network.getComponent(PlatformSecurityNetworkComponent.class));
    }

    private Optional<AutocraftingNetworkComponent> getAutocrafting() {
        return context.resolveNetwork().map(network -> network.getComponent(AutocraftingNetworkComponent.class));
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        context.drainEnergy(Platform.INSTANCE.getConfig().getWirelessGrid().getOpenEnergyUsage());
        context.resolveNetwork().ifPresent(network -> watchers.addWatcher(
            watcher,
            actorType,
            network.getComponent(StorageNetworkComponent.class)
        ));
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        context.resolveNetwork().ifPresent(network -> watchers.removeWatcher(
            watcher,
            network.getComponent(StorageNetworkComponent.class)
        ));
    }

    @Override
    public Storage getItemStorage() {
        return getStorage().map(Storage.class::cast).orElseGet(NoopStorage::new);
    }

    @Override
    public boolean isGridActive() {
        final boolean networkActive = context.resolveNetwork()
            .map(network -> network.getComponent(EnergyNetworkComponent.class).getStored() > 0)
            .orElse(false);
        return networkActive && context.isActive();
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        return getStorage().map(storage -> storage.getResources(actorType)).orElse(Collections.emptyList());
    }

    @Override
    public Set<PlatformResourceKey> getAutocraftableResources() {
        return getAutocrafting()
            .map(AutocraftingNetworkComponent::getOutputs)
            .map(outputs -> outputs.stream()
                .filter(PlatformResourceKey.class::isInstance)
                .map(PlatformResourceKey.class::cast)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        return getStorage()
            .flatMap(rootStorage -> getSecurity()
                .map(security -> createGridOperations(resourceType, player, rootStorage, security)))
            .map(operations -> (GridOperations) new WirelessGridOperations(operations, context, watchers))
            .orElseGet(NoopGridOperations::new);
    }

    private GridOperations createGridOperations(final ResourceType resourceType,
                                                final ServerPlayer player,
                                                final StorageNetworkComponent rootStorage,
                                                final PlatformSecurityNetworkComponent securityNetworkComponent) {
        final PlayerActor playerActor = new PlayerActor(player);
        final GridOperations operations = resourceType.createGridOperations(rootStorage, playerActor);
        return new SecuredGridOperations(player, securityNetworkComponent, operations);
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount) {
        return getAutocrafting()
            .map(component -> component.getPreview(resource, amount))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource) {
        return getAutocrafting()
            .map(component -> component.getMaxAmount(resource))
            .orElseGet(() -> CompletableFuture.completedFuture(0L));
    }

    @Override
    public CompletableFuture<Optional<TaskId>> startTask(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.startTask(resource, amount, actor, notify))
            .orElse(CompletableFuture.completedFuture(Optional.empty()));
    }
}
