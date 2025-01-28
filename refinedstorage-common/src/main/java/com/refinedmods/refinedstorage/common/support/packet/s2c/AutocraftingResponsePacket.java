package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingResponsePacket(UUID id, boolean success) implements CustomPacketPayload {
    public static final Type<AutocraftingResponsePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_response")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingResponsePacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AutocraftingResponsePacket::id,
            ByteBufCodecs.BOOL, AutocraftingResponsePacket::success,
            AutocraftingResponsePacket::new
        );

    public static void handle(final AutocraftingResponsePacket packet) {
        ClientPlatformUtil.autocraftingResponseReceived(packet.id, packet.success);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

