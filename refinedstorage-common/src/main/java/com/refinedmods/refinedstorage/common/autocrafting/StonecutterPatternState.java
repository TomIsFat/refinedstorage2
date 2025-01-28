package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record StonecutterPatternState(ItemResource input, ItemResource selectedOutput) {
    public static final Codec<StonecutterPatternState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceCodecs.ITEM_CODEC.fieldOf("input").forGetter(StonecutterPatternState::input),
        ResourceCodecs.ITEM_CODEC.fieldOf("selectedOutput").forGetter(StonecutterPatternState::selectedOutput)
    ).apply(instance, StonecutterPatternState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StonecutterPatternState> STREAM_CODEC =
        StreamCodec.composite(
            ResourceCodecs.ITEM_STREAM_CODEC, StonecutterPatternState::input,
            ResourceCodecs.ITEM_STREAM_CODEC, StonecutterPatternState::selectedOutput,
            StonecutterPatternState::new
        );
}
