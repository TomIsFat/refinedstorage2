package com.refinedmods.refinedstorage.common.support.widget;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class ProgressWidget extends AbstractWidget {
    private static final ResourceLocation SPRITE = createIdentifier("widget/progress_bar");

    private final DoubleSupplier progressSupplier;
    private final Supplier<List<Component>> tooltipSupplier;

    public ProgressWidget(final int x,
                          final int y,
                          final int width,
                          final int height,
                          final DoubleSupplier progressSupplier,
                          final Supplier<List<Component>> tooltipSupplier) {
        super(x, y, width, height, Component.empty());
        this.progressSupplier = progressSupplier;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void playDownSound(final SoundManager handler) {
        // intentionally empty
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        final int correctedHeight = (int) (progressSupplier.getAsDouble() * height);
        final int correctedY = getY() + height - correctedHeight;
        final int u = 0;
        final int v = height - correctedHeight;
        graphics.blitSprite(SPRITE, 16, 70, u, v, getX(), correctedY, width, correctedHeight);
        if (isHovered) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltipSupplier.get(), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // intentionally empty
    }
}
