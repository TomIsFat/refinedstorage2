package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocrafterNameUpdatePacket(Component name) implements CustomPacketPayload {
    public static final Type<AutocrafterNameUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafter_name_update")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocrafterNameUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, AutocrafterNameUpdatePacket::name,
            AutocrafterNameUpdatePacket::new
        );

    public static void handle(final AutocrafterNameUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AutocrafterContainerMenu containerMenu) {
            containerMenu.nameChanged(packet.name);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
