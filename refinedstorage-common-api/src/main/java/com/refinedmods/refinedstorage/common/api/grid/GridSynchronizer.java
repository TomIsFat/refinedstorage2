package com.refinedmods.refinedstorage.common.api.grid;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface GridSynchronizer {
    MutableComponent getTitle();

    Component getHelpText();

    void synchronizeFromGrid(String text);

    @Nullable
    String getTextToSynchronizeToGrid();

    ResourceLocation getSprite();
}
