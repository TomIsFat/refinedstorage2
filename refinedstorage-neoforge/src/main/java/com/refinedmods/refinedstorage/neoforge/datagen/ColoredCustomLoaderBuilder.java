package com.refinedmods.refinedstorage.neoforge.datagen;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

class ColoredCustomLoaderBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    private final DyeColor color;

    ColoredCustomLoaderBuilder(final ResourceLocation loaderId,
                               final T parent,
                               final ExistingFileHelper existingFileHelper,
                               final DyeColor color) {
        super(loaderId, parent, existingFileHelper, true);
        this.color = color;
    }

    @Override
    public JsonObject toJson(final JsonObject json) {
        final JsonObject value = super.toJson(json);
        value.addProperty("color", color.getName());
        return value;
    }
}
