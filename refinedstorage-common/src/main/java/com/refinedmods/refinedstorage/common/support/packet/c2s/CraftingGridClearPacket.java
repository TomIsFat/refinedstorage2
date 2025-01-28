package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.grid.AbstractCraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record CraftingGridClearPacket(boolean toPlayerInventory) implements CustomPacketPayload {
    public static final Type<CraftingGridClearPacket> PACKET_TYPE = new Type<>(createIdentifier("crafting_grid_clear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingGridClearPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, CraftingGridClearPacket::toPlayerInventory,
            CraftingGridClearPacket::new
        );

    public static void handle(final CraftingGridClearPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractCraftingGridContainerMenu craftingGridContainerMenu) {
            craftingGridContainerMenu.clear(packet.toPlayerInventory());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
