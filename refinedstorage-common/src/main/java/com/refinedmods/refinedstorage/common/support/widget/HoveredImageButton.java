package com.refinedmods.refinedstorage.common.support.widget;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class HoveredImageButton extends Button {
    protected WidgetSprites sprites;

    public HoveredImageButton(final int x,
                              final int y,
                              final int width,
                              final int height,
                              final WidgetSprites sprites,
                              final Consumer<HoveredImageButton> onPress,
                              final Component component) {
        super(x, y, width, height, component, button -> onPress.accept((HoveredImageButton) button), DEFAULT_NARRATION);
        this.sprites = sprites;
    }

    public void setSprites(final WidgetSprites sprites) {
        this.sprites = sprites;
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int x, final int y, final float partialTicks) {
        // only takes isHovered in account, not isFocused
        final ResourceLocation location = sprites.get(isActive(), isHovered());
        graphics.blitSprite(location, getX(), getY(), width, height);
    }
}
