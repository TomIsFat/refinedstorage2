package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallTextClientTooltipComponent;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING;
import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractSideButtonWidget extends Button {
    public static final int SIZE = 18;

    private static final ResourceLocation SPRITE = createIdentifier("widget/side_button/base");
    private static final ResourceLocation HOVERED_SPRITE = createIdentifier("widget/side_button/hovered");
    private static final ResourceLocation HOVER_OVERLAY_SPRITE = createIdentifier("widget/side_button/hover_overlay");

    private static final int ICON_SIZE = 16;

    @Nullable
    private ClientTooltipComponent warning;

    protected AbstractSideButtonWidget(final OnPress pressAction) {
        super(-1, -1, SIZE, SIZE, Component.empty(), pressAction, DEFAULT_NARRATION);
    }

    protected abstract ResourceLocation getSprite();

    public void setWarning(@Nullable final Component text) {
        if (text == null) {
            this.warning = null;
            return;
        }
        this.warning = new SmallTextClientTooltipComponent(text.copy().withStyle(ChatFormatting.RED));
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        graphics.blitSprite(isHovered ? HOVERED_SPRITE : SPRITE, getX(), getY(), SIZE, SIZE);
        graphics.blitSprite(
            getSprite(),
            getX() + 1,
            getY() + 1,
            ICON_SIZE,
            ICON_SIZE
        );
        if (isHovered) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
            graphics.blitSprite(HOVER_OVERLAY_SPRITE, getX(), getY(), SIZE, SIZE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            final Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof AbstractBaseScreen<?> baseScreen) {
                baseScreen.setDeferredTooltip(buildTooltip());
            }
        }
        if (warning != null) {
            renderWarning(graphics);
        }
    }

    private void renderWarning(final GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        graphics.blitSprite(
            WARNING,
            getX() + SIZE - WARNING_SIZE + 2,
            getY() + SIZE - WARNING_SIZE + 2,
            WARNING_SIZE,
            WARNING_SIZE
        );
        graphics.pose().popPose();
    }

    protected List<ClientTooltipComponent> buildTooltip() {
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        final ClientTooltipComponent title = ClientTooltipComponent.create(
            getTitle().getVisualOrderText()
        );
        lines.add(title);
        getSubText().forEach(line -> lines.add(ClientTooltipComponent.create(line.getVisualOrderText())));
        if (warning != null) {
            lines.add(warning);
        }
        final Component helpText = getHelpText();
        if (helpText != null) {
            lines.add(HelpClientTooltipComponent.create(helpText));
        }
        return lines;
    }

    protected abstract MutableComponent getTitle();

    protected abstract List<MutableComponent> getSubText();

    @Nullable
    protected Component getHelpText() {
        return null;
    }
}
