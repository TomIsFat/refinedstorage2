package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AbstractAutocraftingMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorStreamCodecs;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingMonitorCancelPacket(TaskId taskId) implements CustomPacketPayload {
    public static final Type<AutocraftingMonitorCancelPacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_monitor_cancel")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingMonitorCancelPacket> STREAM_CODEC =
        StreamCodec.composite(
            AutocraftingMonitorStreamCodecs.TASK_ID_STREAM_CODEC, AutocraftingMonitorCancelPacket::taskId,
            AutocraftingMonitorCancelPacket::new
        );

    public static void handle(final AutocraftingMonitorCancelPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractAutocraftingMonitorContainerMenu containerMenu) {
            containerMenu.cancelTask(packet.taskId());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
