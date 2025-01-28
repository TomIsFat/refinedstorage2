package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class StorageBlockCodecs {
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Optional<ResourceAmount>>> RESOURCES_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.optional(ResourceCodecs.AMOUNT_STREAM_CODEC));
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageBlockData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, StorageBlockData::stored,
        ByteBufCodecs.VAR_LONG, StorageBlockData::capacity,
        RESOURCES_STREAM_CODEC, StorageBlockData::resources,
        StorageBlockData::new
    );

    private StorageBlockCodecs() {
    }
}
