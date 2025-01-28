package com.refinedmods.refinedstorage.fabric.autocrafting;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public class PatternUnbakedModel implements UnbakedModel {
    private static final ResourceLocation EMPTY_MODEL = createIdentifier("item/pattern/empty");
    private static final ResourceLocation CRAFTING_MODEL = createIdentifier("item/pattern/crafting");
    private static final ResourceLocation PROCESSING_MODEL = createIdentifier("item/pattern/processing");
    private static final ResourceLocation STONECUTTER_MODEL = createIdentifier("item/pattern/stonecutter");
    private static final ResourceLocation SMITHING_TABLE_MODEL = createIdentifier("item/pattern/smithing_table");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Set.of(EMPTY_MODEL, CRAFTING_MODEL, PROCESSING_MODEL, SMITHING_TABLE_MODEL);
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> resolver) {
        resolver.apply(EMPTY_MODEL).resolveParents(resolver);
        resolver.apply(CRAFTING_MODEL).resolveParents(resolver);
        resolver.apply(PROCESSING_MODEL).resolveParents(resolver);
        resolver.apply(STONECUTTER_MODEL).resolveParents(resolver);
        resolver.apply(SMITHING_TABLE_MODEL).resolveParents(resolver);
    }

    @Nullable
    @Override
    public BakedModel bake(final ModelBaker baker,
                           final Function<Material, TextureAtlasSprite> spriteGetter,
                           final ModelState state) {
        return new PatternBakedModel(
            baker,
            requireNonNull(baker.bake(EMPTY_MODEL, state)),
            requireNonNull(baker.bake(CRAFTING_MODEL, state)),
            requireNonNull(baker.bake(PROCESSING_MODEL, state)),
            requireNonNull(baker.bake(STONECUTTER_MODEL, state)),
            requireNonNull(baker.bake(SMITHING_TABLE_MODEL, state))
        );
    }
}
