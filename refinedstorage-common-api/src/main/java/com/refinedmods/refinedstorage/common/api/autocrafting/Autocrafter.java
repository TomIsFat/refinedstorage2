package com.refinedmods.refinedstorage.common.api.autocrafting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.9")
public interface Autocrafter {
    Component getAutocrafterName();

    Container getPatternContainer();

    boolean isVisibleToTheAutocrafterManager();

    BlockPos getLocalPosition();
}
