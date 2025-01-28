package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.autocrafting.monitor.AbstractAutocraftingMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingMonitorCancelAllPacket() implements CustomPacketPayload {
    public static final Type<AutocraftingMonitorCancelAllPacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_monitor_cancel_all")
    );
    public static final AutocraftingMonitorCancelAllPacket INSTANCE = new AutocraftingMonitorCancelAllPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingMonitorCancelAllPacket> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);

    public static void handle(final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractAutocraftingMonitorContainerMenu containerMenu) {
            containerMenu.cancelAllTasks();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
