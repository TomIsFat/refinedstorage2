package com.refinedmods.refinedstorage.common.grid.view;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage.api.grid.view.GridView;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.grid.view.AbstractPlatformGridResource;
import com.refinedmods.refinedstorage.common.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.common.support.tooltip.MouseClientTooltipComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FluidGridResource extends AbstractPlatformGridResource<FluidResource> {
    private static final ItemStack EMPTY_BUCKET = new ItemStack(Items.BUCKET);

    private final int id;
    private final ResourceRendering rendering;

    public FluidGridResource(final FluidResource resource,
                             final String name,
                             final Map<GridResourceAttributeKey, Set<String>> attributes,
                             final boolean autocraftable) {
        super(resource, name, attributes, autocraftable);
        this.id = BuiltInRegistries.FLUID.getId(resource.fluid());
        this.rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(FluidResource.class);
    }

    @Override
    public int getRegistryId() {
        return id;
    }

    @Override
    public List<ClientTooltipComponent> getExtractionHints(final ItemStack carriedStack, final GridView view) {
        return tryFillFluidContainer(carriedStack)
            .filter(result -> result.amount() > 0)
            .map(result -> MouseClientTooltipComponent.item(
                MouseClientTooltipComponent.Type.LEFT,
                result.container(),
                null
            )).stream().toList();
    }

    @Nullable
    @Override
    public ResourceAmount getAutocraftingRequest() {
        return new ResourceAmount(resource, Platform.INSTANCE.getBucketAmount());
    }

    private Optional<FluidOperationResult> tryFillFluidContainer(final ItemStack carriedStack) {
        final ResourceAmount toFill = new ResourceAmount(resource, Platform.INSTANCE.getBucketAmount());
        return carriedStack.isEmpty()
            ? Platform.INSTANCE.fillContainer(EMPTY_BUCKET, toFill)
            : Platform.INSTANCE.fillContainer(carriedStack, toFill);
    }

    @Override
    public boolean canExtract(final ItemStack carriedStack, final GridView view) {
        if (getAmount(view) == 0) {
            return false;
        }
        if (carriedStack.isEmpty()) {
            return true;
        }
        final ResourceAmount toFill = new ResourceAmount(resource, view.getAmount(resource));
        return Platform.INSTANCE.fillContainer(carriedStack, toFill)
            .map(result -> result.amount() > 0)
            .orElse(false);
    }

    @Override
    public void onExtract(final GridExtractMode extractMode,
                          final boolean cursor,
                          final GridExtractionStrategy extractionStrategy) {
        extractionStrategy.onExtract(resource, extractMode, cursor);
    }

    @Override
    public void onScroll(final GridScrollMode scrollMode, final GridScrollingStrategy scrollingStrategy) {
        // no-op
    }

    @Override
    public void render(final GuiGraphics graphics, final int x, final int y) {
        Platform.INSTANCE.getFluidRenderer().render(graphics.pose(), x, y, resource);
    }

    @Override
    public String getDisplayedAmount(final GridView view) {
        return rendering.formatAmount(getAmount(view), true);
    }

    @Override
    public String getAmountInTooltip(final GridView view) {
        return rendering.formatAmount(getAmount(view));
    }

    @Override
    public boolean belongsToResourceType(final ResourceType resourceType) {
        return resourceType == ResourceTypes.FLUID;
    }

    @Override
    public List<Component> getTooltip() {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(resource);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage() {
        return Optional.empty();
    }
}
