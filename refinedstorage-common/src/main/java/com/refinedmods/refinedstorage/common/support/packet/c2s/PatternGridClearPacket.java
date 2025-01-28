package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record PatternGridClearPacket() implements CustomPacketPayload {
    public static final Type<PatternGridClearPacket> PACKET_TYPE = new Type<>(createIdentifier("pattern_grid_clear"));
    public static final PatternGridClearPacket INSTANCE = new PatternGridClearPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridClearPacket> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);

    public static void handle(final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PatternGridContainerMenu patternGridContainerMenu) {
            patternGridContainerMenu.clear();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
