package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.autocrafting.monitor.AbstractAutocraftingMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingMonitorActivePacket(boolean active) implements CustomPacketPayload {
    public static final Type<AutocraftingMonitorActivePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_monitor")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingMonitorActivePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, AutocraftingMonitorActivePacket::active,
            AutocraftingMonitorActivePacket::new
        );

    public static void handle(final AutocraftingMonitorActivePacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof AbstractAutocraftingMonitorContainerMenu containerMenu) {
            containerMenu.activeChanged(packet.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
