package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterContainerMenu;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterData;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record NetworkTransmitterStatusPacket(boolean error, boolean transmitting, Component message)
    implements CustomPacketPayload {
    public static final Type<NetworkTransmitterStatusPacket> PACKET_TYPE = new Type<>(
        createIdentifier("network_transmitter_status")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkTransmitterStatusPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, NetworkTransmitterStatusPacket::error,
            ByteBufCodecs.BOOL, NetworkTransmitterStatusPacket::transmitting,
            ComponentSerialization.STREAM_CODEC, NetworkTransmitterStatusPacket::message,
            NetworkTransmitterStatusPacket::new
        );

    public static void handle(final NetworkTransmitterStatusPacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof NetworkTransmitterContainerMenu containerMenu) {
            containerMenu.setStatus(new NetworkTransmitterData(packet.error, packet.transmitting, packet.message));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
