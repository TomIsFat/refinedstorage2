package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record PatternGridProcessingRecipeTransferPacket(List<List<ResourceAmount>> inputs,
                                                        List<List<ResourceAmount>> outputs)
    implements CustomPacketPayload {
    public static final Type<PatternGridProcessingRecipeTransferPacket> PACKET_TYPE = new Type<>(
        createIdentifier("pattern_grid_processing_recipe_transfer")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridProcessingRecipeTransferPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new,
                ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.AMOUNT_STREAM_CODEC)),
            PatternGridProcessingRecipeTransferPacket::inputs,
            ByteBufCodecs.collection(ArrayList::new,
                ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.AMOUNT_STREAM_CODEC)),
            PatternGridProcessingRecipeTransferPacket::outputs,
            PatternGridProcessingRecipeTransferPacket::new
        );

    public static void handle(final PatternGridProcessingRecipeTransferPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof PatternGridContainerMenu menu) {
            menu.transferProcessingRecipe(packet.inputs, packet.outputs);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
