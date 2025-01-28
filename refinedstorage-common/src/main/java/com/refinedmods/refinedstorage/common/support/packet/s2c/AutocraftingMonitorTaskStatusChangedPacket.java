package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AbstractAutocraftingMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorStreamCodecs;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingMonitorTaskStatusChangedPacket(TaskStatus taskStatus) implements CustomPacketPayload {
    public static final Type<AutocraftingMonitorTaskStatusChangedPacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_monitor_task_status_changed")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingMonitorTaskStatusChangedPacket> STREAM_CODEC =
        StreamCodec.composite(
            AutocraftingMonitorStreamCodecs.STATUS_STREAM_CODEC, AutocraftingMonitorTaskStatusChangedPacket::taskStatus,
            AutocraftingMonitorTaskStatusChangedPacket::new
        );

    public static void handle(final AutocraftingMonitorTaskStatusChangedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractAutocraftingMonitorContainerMenu containerMenu) {
            containerMenu.taskStatusChanged(packet.taskStatus());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

