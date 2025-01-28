package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.inventory.Slot;

interface PatternGridRenderer {
    default void addWidgets(Consumer<AbstractWidget> widgets,
                            Consumer<AbstractWidget> renderables) {
        // no op
    }

    default void tick() {
        // no op
    }

    default void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // no op
    }

    int getClearButtonX();

    int getClearButtonY();

    void renderBackground(GuiGraphics graphics,
                          float partialTicks,
                          int mouseX,
                          int mouseY);

    default void renderTooltip(Font font,
                               @Nullable Slot hoveredSlot,
                               GuiGraphics graphics,
                               int mouseX,
                               int mouseY) {
        // no op
    }

    default void renderLabels(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        // no op
    }

    default boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        return false;
    }

    default void mouseMoved(double mouseX, double mouseY) {
        // no op
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double mouseZ, double delta) {
        return false;
    }

    default void patternTypeChanged(PatternType newPatternType) {
        // no op
    }

    default void fuzzyModeChanged(boolean newFuzzyMode) {
        // no op
    }

    default boolean canInteractWithResourceSlot(ResourceSlot resourceSlot,
                                                double mouseX,
                                                double mouseY) {
        return true;
    }
}
