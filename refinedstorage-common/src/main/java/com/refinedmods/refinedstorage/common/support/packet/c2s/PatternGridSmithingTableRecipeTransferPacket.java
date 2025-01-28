package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record PatternGridSmithingTableRecipeTransferPacket(List<ItemResource> template,
                                                           List<ItemResource> base,
                                                           List<ItemResource> addition)
    implements CustomPacketPayload {
    public static final Type<PatternGridSmithingTableRecipeTransferPacket> PACKET_TYPE = new Type<>(
        createIdentifier("pattern_grid_smithing_table_recipe_transfer")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridSmithingTableRecipeTransferPacket>
        STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.ITEM_STREAM_CODEC),
            PatternGridSmithingTableRecipeTransferPacket::template,
            ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.ITEM_STREAM_CODEC),
            PatternGridSmithingTableRecipeTransferPacket::base,
            ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.ITEM_STREAM_CODEC),
            PatternGridSmithingTableRecipeTransferPacket::addition,
            PatternGridSmithingTableRecipeTransferPacket::new
        );

    public static void handle(final PatternGridSmithingTableRecipeTransferPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PatternGridContainerMenu menu) {
            menu.transferSmithingTableRecipe(packet.template, packet.base, packet.addition);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
