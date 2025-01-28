package com.refinedmods.refinedstorage.neoforge.storage.diskinterface;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import static com.refinedmods.refinedstorage.common.content.Blocks.COLOR;

public class DiskInterfaceGeometryLoader implements IGeometryLoader<DiskInterfaceUnbakedGeometry> {
    @Override
    public DiskInterfaceUnbakedGeometry read(final JsonObject jsonObject,
                                             final JsonDeserializationContext deserializationContext) {
        final String color = jsonObject.get("color").getAsString();
        final DyeColor dyeColor = DyeColor.byName(color, COLOR);
        return new DiskInterfaceUnbakedGeometry(dyeColor);
    }
}
