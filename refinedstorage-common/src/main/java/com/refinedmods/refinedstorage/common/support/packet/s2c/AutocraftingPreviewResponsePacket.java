package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewItem;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.PlatformUtil.enumStreamCodec;

public record AutocraftingPreviewResponsePacket(UUID id, Preview preview) implements CustomPacketPayload {
    public static final Type<AutocraftingPreviewResponsePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_preview_response")
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, PreviewItem> PREVIEW_ITEM_STREAM_CODEC =
        StreamCodec.composite(
            ResourceCodecs.STREAM_CODEC, item -> (PlatformResourceKey) item.resource(),
            ByteBufCodecs.VAR_LONG, PreviewItem::available,
            ByteBufCodecs.VAR_LONG, PreviewItem::missing,
            ByteBufCodecs.VAR_LONG, PreviewItem::toCraft,
            PreviewItem::new
        );
    private static final StreamCodec<RegistryFriendlyByteBuf, Preview> PREVIEW_STREAM_CODEC =
        StreamCodec.composite(
            enumStreamCodec(PreviewType.values()), Preview::type,
            ByteBufCodecs.collection(ArrayList::new, PREVIEW_ITEM_STREAM_CODEC), Preview::items,
            ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.AMOUNT_STREAM_CODEC),
            Preview::outputsOfPatternWithCycle,
            Preview::new
        );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingPreviewResponsePacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AutocraftingPreviewResponsePacket::id,
            PREVIEW_STREAM_CODEC, AutocraftingPreviewResponsePacket::preview,
            AutocraftingPreviewResponsePacket::new
        );

    public static void handle(final AutocraftingPreviewResponsePacket packet) {
        ClientPlatformUtil.autocraftingPreviewResponseReceived(packet.id, packet.preview);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

