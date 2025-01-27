package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocrafterLockedUpdatePacket(boolean locked) implements CustomPacketPayload {
    public static final Type<AutocrafterLockedUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafter_locked_update")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocrafterLockedUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, AutocrafterLockedUpdatePacket::locked,
            AutocrafterLockedUpdatePacket::new
        );

    public static void handle(final AutocrafterLockedUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AutocrafterContainerMenu containerMenu) {
            containerMenu.lockedChanged(packet.locked);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
