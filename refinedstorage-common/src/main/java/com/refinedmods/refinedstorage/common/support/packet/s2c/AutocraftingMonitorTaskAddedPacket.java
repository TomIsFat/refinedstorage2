package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AbstractAutocraftingMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorStreamCodecs;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingMonitorTaskAddedPacket(TaskStatus taskStatus) implements CustomPacketPayload {
    public static final Type<AutocraftingMonitorTaskAddedPacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_monitor_task_added")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingMonitorTaskAddedPacket> STREAM_CODEC =
        StreamCodec.composite(
            AutocraftingMonitorStreamCodecs.STATUS_STREAM_CODEC, AutocraftingMonitorTaskAddedPacket::taskStatus,
            AutocraftingMonitorTaskAddedPacket::new
        );

    public static void handle(final AutocraftingMonitorTaskAddedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractAutocraftingMonitorContainerMenu containerMenu) {
            containerMenu.taskAdded(packet.taskStatus());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

