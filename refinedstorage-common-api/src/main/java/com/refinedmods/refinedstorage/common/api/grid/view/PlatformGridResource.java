package com.refinedmods.refinedstorage.common.api.grid.view;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.grid.view.GridView;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface PlatformGridResource extends GridResource {
    boolean canExtract(ItemStack carriedStack, GridView view);

    void onExtract(GridExtractMode extractMode,
                   boolean cursor,
                   GridExtractionStrategy extractionStrategy);

    void onScroll(GridScrollMode scrollMode,
                  GridScrollingStrategy scrollingStrategy);

    void render(GuiGraphics graphics, int x, int y);

    String getDisplayedAmount(GridView view);

    String getAmountInTooltip(GridView view);

    boolean belongsToResourceType(ResourceType resourceType);

    List<Component> getTooltip();

    Optional<TooltipComponent> getTooltipImage();

    int getRegistryId();

    List<ClientTooltipComponent> getExtractionHints(ItemStack carriedStack, GridView view);

    @Nullable
    ResourceAmount getAutocraftingRequest();

    @Nullable
    @API(status = API.Status.INTERNAL)
    PlatformResourceKey getResourceForRecipeMods();
}
