package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record FilterSlotChangePacket(int slotIndex, ItemStack stack) implements CustomPacketPayload {
    public static final Type<FilterSlotChangePacket> PACKET_TYPE = new Type<>(
        createIdentifier("filter_slot_change")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterSlotChangePacket> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.INT, FilterSlotChangePacket::slotIndex,
            ItemStack.STREAM_CODEC, FilterSlotChangePacket::stack,
            FilterSlotChangePacket::new
        );

    public static void handle(final FilterSlotChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractBaseContainerMenu containerMenu) {
            containerMenu.handleFilterSlotChange(packet.slotIndex, packet.stack);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
