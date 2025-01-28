package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

record ProcessingInputData(ResourceContainerData resourceContainerData, List<Set<ResourceLocation>> allowedTagIds) {
    static final StreamCodec<RegistryFriendlyByteBuf, ProcessingInputData> STREAM_CODEC = StreamCodec.composite(
        ResourceContainerData.STREAM_CODEC, ProcessingInputData::resourceContainerData,
        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.collection(HashSet::new, ResourceLocation.STREAM_CODEC)),
        ProcessingInputData::allowedTagIds,
        ProcessingInputData::new
    );

    static ProcessingInputData of(final ProcessingMatrixInputResourceContainer resourceContainer) {
        final List<Set<ResourceLocation>> allowedTagIds = new ArrayList<>();
        for (int i = 0; i < resourceContainer.size(); ++i) {
            allowedTagIds.add(resourceContainer.getAllowedTagIds(i));
        }
        return new ProcessingInputData(
            ResourceContainerData.of(resourceContainer),
            allowedTagIds
        );
    }
}
