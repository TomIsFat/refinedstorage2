package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record PatternGridStonecutterRecipeTransferPacket(ItemResource input,
                                                         ItemResource selectedOutput)
    implements CustomPacketPayload {
    public static final Type<PatternGridStonecutterRecipeTransferPacket> PACKET_TYPE = new Type<>(
        createIdentifier("pattern_grid_stonecutter_recipe_transfer")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridStonecutterRecipeTransferPacket> STREAM_CODEC =
        StreamCodec.composite(
            ResourceCodecs.ITEM_STREAM_CODEC,
            PatternGridStonecutterRecipeTransferPacket::input,
            ResourceCodecs.ITEM_STREAM_CODEC,
            PatternGridStonecutterRecipeTransferPacket::selectedOutput,
            PatternGridStonecutterRecipeTransferPacket::new
        );

    public static void handle(final PatternGridStonecutterRecipeTransferPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PatternGridContainerMenu menu) {
            menu.transferStonecutterRecipe(packet.input, packet.selectedOutput);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
