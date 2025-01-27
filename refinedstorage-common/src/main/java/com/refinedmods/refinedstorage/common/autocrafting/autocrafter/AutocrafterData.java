package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AutocrafterData(boolean partOfChain, boolean headOfChain, boolean locked) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocrafterData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, AutocrafterData::partOfChain,
        ByteBufCodecs.BOOL, AutocrafterData::headOfChain,
        ByteBufCodecs.BOOL, AutocrafterData::locked,
        AutocrafterData::new
    );
}
