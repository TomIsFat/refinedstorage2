package com.refinedmods.refinedstorage.fabric.networking;

import com.refinedmods.refinedstorage.fabric.support.render.QuadRotators;

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
import net.minecraft.world.item.DyeColor;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static java.util.Objects.requireNonNull;

public class CableUnbakedModel implements UnbakedModel {
    private final QuadRotators quadRotators;
    private final ResourceLocation coreModel;
    private final ResourceLocation extensionModel;

    public CableUnbakedModel(final QuadRotators quadRotators, final DyeColor color) {
        this.quadRotators = quadRotators;
        this.coreModel = createIdentifier("block/cable/core/" + color.getName());
        this.extensionModel = createIdentifier("block/cable/extension/" + color.getName());
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Set.of(coreModel, extensionModel);
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> resolver) {
        resolver.apply(coreModel).resolveParents(resolver);
        resolver.apply(extensionModel).resolveParents(resolver);
    }

    @Nullable
    @Override
    public BakedModel bake(final ModelBaker baker,
                           final Function<Material, TextureAtlasSprite> spriteGetter,
                           final ModelState state) {
        return new CableBakedModel(
            quadRotators,
            requireNonNull(baker.bake(coreModel, state)),
            requireNonNull(baker.bake(extensionModel, state))
        );
    }
}
