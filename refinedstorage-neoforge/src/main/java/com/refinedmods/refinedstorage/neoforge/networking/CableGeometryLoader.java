package com.refinedmods.refinedstorage.neoforge.networking;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import static com.refinedmods.refinedstorage.common.content.Blocks.CABLE_LIKE_COLOR;

public class CableGeometryLoader implements IGeometryLoader<CableUnbakedGeometry> {
    @Override
    public CableUnbakedGeometry read(final JsonObject jsonObject,
                                     final JsonDeserializationContext jsonDeserializationContext)
        throws JsonParseException {
        final String color = jsonObject.get("color").getAsString();
        final DyeColor dyeColor = DyeColor.byName(color, CABLE_LIKE_COLOR);
        return new CableUnbakedGeometry(dyeColor);
    }
}
