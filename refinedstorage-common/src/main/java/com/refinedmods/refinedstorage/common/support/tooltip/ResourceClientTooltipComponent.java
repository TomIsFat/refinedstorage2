package com.refinedmods.refinedstorage.common.support.tooltip;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class ResourceClientTooltipComponent implements ClientTooltipComponent {
    private final ResourceAmount resourceAmount;
    private final Component name;

    public ResourceClientTooltipComponent(final ResourceAmount resourceAmount) {
        this.resourceAmount = resourceAmount;
        this.name = getNameWithAmount(resourceAmount);
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth(final Font font) {
        return 16 + 4 + font.width(name);
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        RefinedStorageClientApi.INSTANCE.getResourceRendering(resourceAmount.resource().getClass()).render(
            resourceAmount.resource(),
            graphics,
            x,
            y
        );
        graphics.drawString(
            font,
            name,
            x + 16 + 4,
            y + 4,
            0xAAAAAA
        );
    }

    private static Component getNameWithAmount(final ResourceAmount resourceAmount) {
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            resourceAmount.resource().getClass()
        );
        final String amount = rendering.formatAmount(resourceAmount.amount());
        final Component displayName = rendering.getDisplayName(resourceAmount.resource());
        return displayName.copy().append(" (").append(amount).append(")");
    }
}
