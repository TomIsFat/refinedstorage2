package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocrafterNameChangePacket(String name) implements CustomPacketPayload {
    public static final Type<AutocrafterNameChangePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafter_name_change")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocrafterNameChangePacket> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.STRING_UTF8, AutocrafterNameChangePacket::name,
            AutocrafterNameChangePacket::new
        );

    public static void handle(final AutocrafterNameChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AutocrafterContainerMenu containerMenu) {
            containerMenu.changeName(packet.name);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
