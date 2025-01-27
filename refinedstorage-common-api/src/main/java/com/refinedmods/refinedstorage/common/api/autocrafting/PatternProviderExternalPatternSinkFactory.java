package com.refinedmods.refinedstorage.common.api.autocrafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
@FunctionalInterface
public interface PatternProviderExternalPatternSinkFactory {
    PlatformPatternProviderExternalPatternSink create(ServerLevel level, BlockPos pos, Direction direction);
}
