package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.networking.WirelessTransmitterContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record WirelessTransmitterDataPacket(int range, boolean active) implements CustomPacketPayload {
    public static final Type<WirelessTransmitterDataPacket> PACKET_TYPE = new Type<>(
        createIdentifier("wireless_transmitter_data")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessTransmitterDataPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, WirelessTransmitterDataPacket::range,
            ByteBufCodecs.BOOL, WirelessTransmitterDataPacket::active,
            WirelessTransmitterDataPacket::new
        );

    public static void handle(final WirelessTransmitterDataPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof WirelessTransmitterContainerMenu containerMenu) {
            containerMenu.setRange(packet.range);
            containerMenu.setActive(packet.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
