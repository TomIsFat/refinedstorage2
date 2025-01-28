package com.refinedmods.refinedstorage.common.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WirelessTransmitterData(int range, boolean active) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessTransmitterData> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, WirelessTransmitterData::range,
            ByteBufCodecs.BOOL, WirelessTransmitterData::active,
            WirelessTransmitterData::new
        );
}
