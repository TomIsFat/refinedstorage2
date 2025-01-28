package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternType;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PatternState(UUID id, PatternType type) {
    public static final Codec<PatternState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(PatternState::id),
        PatternType.CODEC.fieldOf("type").forGetter(PatternState::type)
    ).apply(instance, PatternState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PatternState> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PatternState::id,
            PlatformUtil.enumStreamCodec(PatternType.values()), PatternState::type,
            PatternState::new
        );
}
