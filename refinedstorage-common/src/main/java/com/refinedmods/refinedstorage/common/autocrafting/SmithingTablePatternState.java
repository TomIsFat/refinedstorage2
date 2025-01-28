package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SmithingTablePatternState(ItemResource template,
                                        ItemResource base,
                                        ItemResource addition) {
    public static final Codec<SmithingTablePatternState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceCodecs.ITEM_CODEC.fieldOf("template").forGetter(SmithingTablePatternState::template),
        ResourceCodecs.ITEM_CODEC.fieldOf("base").forGetter(SmithingTablePatternState::base),
        ResourceCodecs.ITEM_CODEC.fieldOf("addition").forGetter(SmithingTablePatternState::addition)
    ).apply(instance, SmithingTablePatternState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTablePatternState> STREAM_CODEC =
        StreamCodec.composite(
            ResourceCodecs.ITEM_STREAM_CODEC, SmithingTablePatternState::template,
            ResourceCodecs.ITEM_STREAM_CODEC, SmithingTablePatternState::base,
            ResourceCodecs.ITEM_STREAM_CODEC, SmithingTablePatternState::addition,
            SmithingTablePatternState::new
        );
}
