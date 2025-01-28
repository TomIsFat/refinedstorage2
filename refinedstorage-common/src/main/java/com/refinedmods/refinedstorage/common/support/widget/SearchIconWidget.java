package com.refinedmods.refinedstorage.common.support.widget;

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class SearchIconWidget extends AbstractWidget {
    public static final ResourceLocation SEARCH = createIdentifier("search");
    public static final int SEARCH_SIZE = 12;

    private final Supplier<Component> messageSupplier;
    private final EditBox editBox;

    public SearchIconWidget(final int x,
                            final int y,
                            final Supplier<Component> messageSupplier,
                            final EditBox editBox) {
        super(x, y, SEARCH_SIZE, SEARCH_SIZE, Component.empty());
        this.messageSupplier = messageSupplier;
        this.editBox = editBox;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            editBox.setFocused(true);
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics,
                                final int mouseX,
                                final int mouseY,
                                final float partialTicks) {
        graphics.blitSprite(SEARCH, getX(), getY(), SEARCH_SIZE, SEARCH_SIZE);
        if (isHovered) {
            setTooltip(Tooltip.create(messageSupplier.get()));
        } else {
            setTooltip(null);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // no op
    }
}
