package com.refinedmods.refinedstorage.neoforge.support.render;

import com.refinedmods.refinedstorage.common.support.render.AbstractFluidRenderer;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class FluidStackFluidRenderer extends AbstractFluidRenderer {
    private final Map<FluidResource, FluidStack> stackCache = new HashMap<>();

    private FluidStack getFluidStackFromCache(final FluidResource fluidResource) {
        if (stackCache.size() > 1000) {
            stackCache.clear();
        }
        return stackCache.computeIfAbsent(
            fluidResource,
            fluid -> new FluidStack(
                BuiltInRegistries.FLUID.wrapAsHolder(fluid.fluid()),
                FluidType.BUCKET_VOLUME,
                fluid.components()
            )
        );
    }

    @Override
    public void render(final PoseStack poseStack, final int x, final int y, final FluidResource fluidResource) {
        final FluidStack stack = getFluidStackFromCache(fluidResource);
        final Fluid fluid = fluidResource.fluid();

        final IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);

        final int packedRgb = renderProperties.getTintColor(stack);
        final TextureAtlasSprite sprite = getStillFluidSprite(renderProperties, stack);

        render(poseStack, x, y, packedRgb, sprite);
    }

    @Override
    public void render(final PoseStack poseStack,
                       final MultiBufferSource renderTypeBuffer,
                       final int light,
                       final FluidResource fluidResource) {
        final FluidStack stack = getFluidStackFromCache(fluidResource);
        final Fluid fluid = fluidResource.fluid();

        final IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);

        final int packedRgb = renderProperties.getTintColor(stack);
        final TextureAtlasSprite sprite = getStillFluidSprite(renderProperties, stack);

        render(poseStack, renderTypeBuffer, light, packedRgb, sprite);
    }

    @Override
    public Component getDisplayName(final FluidResource fluidResource) {
        return fluidResource.fluid().getFluidType().getDescription();
    }

    private TextureAtlasSprite getStillFluidSprite(final IClientFluidTypeExtensions renderProperties,
                                                   final FluidStack fluidStack) {
        final Minecraft minecraft = Minecraft.getInstance();
        final ResourceLocation fluidStill = renderProperties.getStillTexture(fluidStack);
        return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
    }

    @Override
    public List<Component> getTooltip(final FluidResource fluidResource) {
        return Collections.singletonList(getFluidStackFromCache(fluidResource).getHoverName());
    }
}
