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

public record PatternGridCraftingRecipeTransferPacket(List<List<ItemResource>> recipe) implements CustomPacketPayload {
    public static final Type<PatternGridCraftingRecipeTransferPacket> PACKET_TYPE = new Type<>(
        createIdentifier("pattern_grid_crafting_recipe_transfer")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridCraftingRecipeTransferPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new,
                ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.ITEM_STREAM_CODEC)),
            PatternGridCraftingRecipeTransferPacket::recipe,
            PatternGridCraftingRecipeTransferPacket::new
        );

    public static void handle(final PatternGridCraftingRecipeTransferPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PatternGridContainerMenu menu) {
            menu.transferCraftingRecipe(packet.recipe());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
