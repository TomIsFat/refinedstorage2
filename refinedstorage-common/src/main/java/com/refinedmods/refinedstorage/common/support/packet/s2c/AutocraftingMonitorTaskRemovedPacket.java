package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AbstractAutocraftingMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorStreamCodecs;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingMonitorTaskRemovedPacket(TaskId taskId) implements CustomPacketPayload {
    public static final Type<AutocraftingMonitorTaskRemovedPacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_monitor_task_removed")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingMonitorTaskRemovedPacket> STREAM_CODEC =
        StreamCodec.composite(
            AutocraftingMonitorStreamCodecs.TASK_ID_STREAM_CODEC, AutocraftingMonitorTaskRemovedPacket::taskId,
            AutocraftingMonitorTaskRemovedPacket::new
        );

    public static void handle(final AutocraftingMonitorTaskRemovedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractAutocraftingMonitorContainerMenu containerMenu) {
            containerMenu.taskRemoved(packet.taskId());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

