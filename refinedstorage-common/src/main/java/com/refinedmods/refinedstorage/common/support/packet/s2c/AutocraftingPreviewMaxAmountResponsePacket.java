package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingPreviewMaxAmountResponsePacket(long maxAmount) implements CustomPacketPayload {
    public static final Type<AutocraftingPreviewMaxAmountResponsePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_preview_max_amount_response")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingPreviewMaxAmountResponsePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, AutocraftingPreviewMaxAmountResponsePacket::maxAmount,
            AutocraftingPreviewMaxAmountResponsePacket::new
        );

    public static void handle(final AutocraftingPreviewMaxAmountResponsePacket packet) {
        ClientPlatformUtil.autocraftingPreviewMaxAmountResponseReceived(packet.maxAmount);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

