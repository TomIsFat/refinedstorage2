package com.refinedmods.refinedstorage.neoforge.networking;

import com.refinedmods.refinedstorage.neoforge.support.render.RotationTranslationModelBaker;

import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public class CableUnbakedGeometry implements IUnbakedGeometry<CableUnbakedGeometry> {
    private final ResourceLocation coreModel;
    private final ResourceLocation extensionModel;

    CableUnbakedGeometry(final DyeColor color) {
        this.coreModel = createIdentifier("block/cable/core/" + color.getName());
        this.extensionModel = createIdentifier("block/cable/extension/" + color.getName());
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter,
                               final IGeometryBakingContext context) {
        modelGetter.apply(coreModel).resolveParents(modelGetter);
        modelGetter.apply(extensionModel).resolveParents(modelGetter);
    }

    @Override
    public BakedModel bake(final IGeometryBakingContext ctx,
                           final ModelBaker modelBaker,
                           final Function<Material, TextureAtlasSprite> function,
                           final ModelState modelState,
                           final ItemOverrides itemOverrides) {
        return new CableBakedModel(
            requireNonNull(modelBaker.bake(coreModel, modelState, function)),
            new RotationTranslationModelBaker(
                modelState, modelBaker, function, extensionModel
            )
        );
    }
}
