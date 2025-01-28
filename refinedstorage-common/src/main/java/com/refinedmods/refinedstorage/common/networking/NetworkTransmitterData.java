package com.refinedmods.refinedstorage.common.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NetworkTransmitterData(boolean error, boolean transmitting, Component message) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkTransmitterData> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, NetworkTransmitterData::error,
            ByteBufCodecs.BOOL, NetworkTransmitterData::transmitting,
            ComponentSerialization.STREAM_CODEC, NetworkTransmitterData::message,
            NetworkTransmitterData::new
        );

    static NetworkTransmitterData error(final Component message) {
        return new NetworkTransmitterData(true, false, message);
    }

    static NetworkTransmitterData message(final boolean transmitting, final Component message) {
        return new NetworkTransmitterData(false, transmitting, message);
    }
}
