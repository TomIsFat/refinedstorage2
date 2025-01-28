package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PatternGridData(GridData gridData,
                              PatternType patternType,
                              ProcessingInputData processingInputData,
                              ResourceContainerData processingOutputData,
                              int stonecutterSelectedRecipe) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternGridData> STREAM_CODEC = StreamCodec.composite(
        GridData.STREAM_CODEC, PatternGridData::gridData,
        PlatformUtil.enumStreamCodec(PatternType.values()), PatternGridData::patternType,
        ProcessingInputData.STREAM_CODEC, PatternGridData::processingInputData,
        ResourceContainerData.STREAM_CODEC, PatternGridData::processingOutputData,
        ByteBufCodecs.INT, PatternGridData::stonecutterSelectedRecipe,
        PatternGridData::new
    );
}
