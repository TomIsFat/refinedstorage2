package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.support.Sprites;

import javax.annotation.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.support.Sprites.ICON_SIZE;

public class IconButton extends Button {
    @Nullable
    private Icon icon;

    public IconButton(final int x,
                      final int y,
                      final int width,
                      final int height,
                      final Component message,
                      final OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics,
                                final int mouseX,
                                final int mouseY,
                                final float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (icon != null) {
            graphics.blitSprite(icon.sprite, getX() + 4, getY() + 4, ICON_SIZE, ICON_SIZE);
        }
    }

    @Override
    protected void renderScrollingString(final GuiGraphics graphics,
                                         final Font font,
                                         final int width,
                                         final int color) {
        final int offset = icon != null ? (ICON_SIZE - 6) : 0;
        final int start = offset + getX() + width;
        final int end = offset + getX() + getWidth() - width;
        renderScrollingString(graphics, font, getMessage(), start, getY(), end, getY() + getHeight(), color);
    }

    public void setIcon(@Nullable final Icon icon) {
        this.icon = icon;
    }

    public enum Icon {
        ERROR(Sprites.ERROR),
        START(Sprites.START),
        CANCEL(Sprites.CANCEL),
        RESET(Sprites.RESET),
        SET(Sprites.SET);

        private final ResourceLocation sprite;

        Icon(final ResourceLocation sprite) {
            this.sprite = sprite;
        }
    }
}
