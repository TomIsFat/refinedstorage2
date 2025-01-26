package com.refinedmods.refinedstorage.common.storagemonitor;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingRequest;
import com.refinedmods.refinedstorage.common.content.Menus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class AutocraftingStorageMonitorContainerMenu extends AutocraftingPreviewContainerMenu
    implements PreviewProvider {
    @Nullable
    private final StorageMonitorBlockEntity storageMonitor;

    public AutocraftingStorageMonitorContainerMenu(final int syncId, final PlatformResourceKey resource) {
        super(Menus.INSTANCE.getAutocraftingStorageMonitor(), syncId, getRequests(resource));
        this.storageMonitor = null;
    }

    AutocraftingStorageMonitorContainerMenu(final int syncId,
                                            final PlatformResourceKey resource,
                                            final StorageMonitorBlockEntity storageMonitor) {
        super(Menus.INSTANCE.getAutocraftingStorageMonitor(), syncId, getRequests(resource));
        this.storageMonitor = storageMonitor;
    }

    private static List<AutocraftingRequest> getRequests(final PlatformResourceKey resource) {
        return List.of(AutocraftingRequest.of(
            new ResourceAmount(resource, resource.getResourceType().normalizeAmount(1.0D))
        ));
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount) {
        return requireNonNull(storageMonitor).getPreview(resource, amount);
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource) {
        return requireNonNull(storageMonitor).getMaxAmount(resource);
    }

    @Override
    public CompletableFuture<Optional<TaskId>> startTask(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify) {
        return requireNonNull(storageMonitor).startTask(resource, amount, actor, notify);
    }
}
