package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocrafterManagerActivePacket(boolean active) implements CustomPacketPayload {
    public static final Type<AutocrafterManagerActivePacket> PACKET_TYPE =
        new Type<>(createIdentifier("autocrafter_manager_active"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocrafterManagerActivePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, AutocrafterManagerActivePacket::active,
            AutocrafterManagerActivePacket::new
        );

    public static void handle(final AutocrafterManagerActivePacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof AutocrafterManagerContainerMenu containerMenu) {
            containerMenu.setActive(packet.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
