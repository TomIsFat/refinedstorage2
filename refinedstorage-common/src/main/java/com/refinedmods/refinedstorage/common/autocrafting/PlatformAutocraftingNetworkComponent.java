package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.network.impl.autocrafting.AutocraftingNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingTaskCompletedPacket;
import com.refinedmods.refinedstorage.common.util.ServerListener;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PlatformAutocraftingNetworkComponent extends AutocraftingNetworkComponentImpl {
    public PlatformAutocraftingNetworkComponent(final Supplier<RootStorage> rootStorageProvider,
                                                final ExecutorService executorService) {
        super(rootStorageProvider, executorService);
    }

    @Override
    public void taskCompleted(final Task task) {
        super.taskCompleted(task);
        if (task.shouldNotify()
            && task.getActor() instanceof PlayerActor(String name)
            && task.getResource() instanceof PlatformResourceKey resource) {
            sendToClient(task, name, resource);
        }
    }

    private static void sendToClient(final Task task, final String name, final PlatformResourceKey resource) {
        ServerListener.queue(server -> sendToClient(task, name, resource, server));
    }

    private static void sendToClient(final Task task,
                                     final String name,
                                     final PlatformResourceKey resource,
                                     final MinecraftServer server) {
        final ServerPlayer player = server.getPlayerList().getPlayerByName(name);
        if (player == null) {
            return;
        }
        Platform.INSTANCE.sendPacketToClient(player, new AutocraftingTaskCompletedPacket(
            resource,
            task.getAmount()
        ));
    }
}
