package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.util.PacketUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PatternGridData(GridData gridData,
                              PatternType patternType,
                              ResourceContainerData processingInputData,
                              ResourceContainerData processingOutputData) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridData> STREAM_CODEC = StreamCodec.composite(
        GridData.STREAM_CODEC, PatternGridData::gridData,
        PacketUtil.enumStreamCodec(PatternType.values()), PatternGridData::patternType,
        ResourceContainerData.STREAM_CODEC, PatternGridData::processingInputData,
        ResourceContainerData.STREAM_CODEC, PatternGridData::processingOutputData,
        PatternGridData::new
    );
}
