package com.refinedmods.refinedstorage.neoforge.autocrafting;

import java.util.function.Function;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public class PatternUnbakedGeometry implements IUnbakedGeometry<PatternUnbakedGeometry> {
    private static final ResourceLocation EMPTY_MODEL = createIdentifier("item/pattern/empty");
    private static final ResourceLocation CRAFTING_MODEL = createIdentifier("item/pattern/crafting");
    private static final ResourceLocation PROCESSING_MODEL = createIdentifier("item/pattern/processing");
    private static final ResourceLocation STONECUTTER_MODEL = createIdentifier("item/pattern/stonecutter");
    private static final ResourceLocation SMITHING_TABLE_MODEL = createIdentifier("item/pattern/smithing_table");

    @Override
    public BakedModel bake(final IGeometryBakingContext ctx,
                           final ModelBaker modelBaker,
                           final Function<Material, TextureAtlasSprite> function,
                           final ModelState modelState,
                           final ItemOverrides itemOverrides) {
        return new PatternBakedModel(
            modelBaker,
            requireNonNull(modelBaker.bake(EMPTY_MODEL, modelState, function)),
            requireNonNull(modelBaker.bake(CRAFTING_MODEL, modelState, function)),
            requireNonNull(modelBaker.bake(PROCESSING_MODEL, modelState, function)),
            requireNonNull(modelBaker.bake(STONECUTTER_MODEL, modelState, function)),
            requireNonNull(modelBaker.bake(SMITHING_TABLE_MODEL, modelState, function))
        );
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter,
                               final IGeometryBakingContext context) {
        modelGetter.apply(EMPTY_MODEL).resolveParents(modelGetter);
        modelGetter.apply(CRAFTING_MODEL).resolveParents(modelGetter);
        modelGetter.apply(PROCESSING_MODEL).resolveParents(modelGetter);
        modelGetter.apply(STONECUTTER_MODEL).resolveParents(modelGetter);
        modelGetter.apply(SMITHING_TABLE_MODEL).resolveParents(modelGetter);
    }
}
