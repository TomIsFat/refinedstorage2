package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;

import java.util.Collections;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_HEIGHT;
import static com.refinedmods.refinedstorage.common.support.Sprites.LIGHT_ARROW_WIDTH;
import static com.refinedmods.refinedstorage.common.support.Sprites.SLOT;

class ProcessingPatternClientTooltipComponent implements ClientTooltipComponent {
    private static final Ingredient EMPTY_INGREDIENT = new Ingredient(0, Collections.emptyList());

    private static final long CYCLE_MS = 1000;
    private static final int ARROW_SPACING = 8;

    private final int rows;
    private final List<Component> outputTexts;
    private final List<Ingredient> ingredients;
    private final List<List<ResourceAmount>> outputs;

    private long cycleStart = 0;
    private int currentCycle = 0;

    ProcessingPatternClientTooltipComponent(final ProcessingPatternState state) {
        this.rows = calculateMaxRows(state);
        this.outputTexts = getOutputText(state);
        this.ingredients = state.ingredients()
            .stream()
            .map(processingIngredient ->
                processingIngredient.map(ProcessingPatternState.ProcessingIngredient::toIngredient)
                    .orElse(EMPTY_INGREDIENT)
            ).toList();
        this.outputs = state.outputs().stream().map(output -> output.map(List::of).orElse(List.of())).toList();
    }

    private static int calculateMaxRows(final ProcessingPatternState state) {
        int lastFilledInputIndex = 0;
        for (int i = 0; i < state.ingredients().size(); i++) {
            if (state.ingredients().get(i).isPresent()) {
                lastFilledInputIndex = i;
            }
        }
        int lastFilledOutputIndex = 0;
        for (int i = 0; i < state.outputs().size(); i++) {
            if (state.outputs().get(i).isPresent()) {
                lastFilledOutputIndex = i;
            }
        }
        final int lastFilledInputRow = Math.ceilDiv(lastFilledInputIndex + 1, 3);
        final int lastFilledOutputRow = Math.ceilDiv(lastFilledOutputIndex + 1, 3);
        return Math.max(lastFilledInputRow, lastFilledOutputRow);
    }

    private static List<Component> getOutputText(final ProcessingPatternState state) {
        return state.getFlatOutputs()
            .stream()
            .map(ProcessingPatternClientTooltipComponent::getOutputText)
            .toList();
    }

    private static Component getOutputText(final ResourceAmount resourceAmount) {
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            resourceAmount.resource().getClass()
        );
        final String displayAmount = rendering.formatAmount(resourceAmount.amount());
        return Component.literal(String.format("%sx ", displayAmount))
            .append(rendering.getDisplayName(resourceAmount.resource()))
            .withStyle(ChatFormatting.GRAY);
    }

    @Override
    public int getHeight() {
        return (outputTexts.size() * 9) + 2 + (rows * 18) + 3;
    }

    @Override
    public int getWidth(final Font font) {
        return (18 * 3) + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING + (18 * 3);
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        final long now = System.currentTimeMillis();
        if (cycleStart == 0) {
            cycleStart = now;
        }
        if (now - cycleStart >= CYCLE_MS) {
            currentCycle++;
            cycleStart = now;
        }
        renderOutputText(font, x, y, graphics);
        final int matrixSlotsY = y + (outputTexts.size() * 9) + 2;
        renderMatrixSlots(x, matrixSlotsY, true, graphics);
        graphics.blitSprite(
            LIGHT_ARROW,
            x + (18 * 3) + ARROW_SPACING,
            y + (outputTexts.size() * 9) + 2 + ((rows * 18) / 2) - (LIGHT_ARROW_HEIGHT / 2),
            LIGHT_ARROW_WIDTH,
            LIGHT_ARROW_HEIGHT
        );
        renderMatrixSlots(x, matrixSlotsY, false, graphics);
    }

    private void renderMatrixSlots(final int x,
                                   final int y,
                                   final boolean input,
                                   final GuiGraphics graphics) {
        final int maxSize = input ? ingredients.size() : outputs.size();
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < 3; ++column) {
                final int slotXOffset = !input ? ((18 * 3) + ARROW_SPACING + LIGHT_ARROW_WIDTH + ARROW_SPACING) : 0;
                final int slotX = x + slotXOffset + column * 18;
                final int slotY = y + row * 18;
                final int idx = row * 3 + column;
                if (idx >= maxSize) {
                    break;
                }
                if (input) {
                    renderMatrixSlot(graphics, slotX, slotY, ingredients.get(idx));
                } else {
                    renderMatrixSlot(graphics, slotX, slotY, outputs.get(idx));
                }
            }
        }
    }

    private void renderMatrixSlot(
        final GuiGraphics graphics,
        final int slotX,
        final int slotY,
        final Ingredient ingredient
    ) {
        graphics.blitSprite(SLOT, slotX, slotY, 18, 18);
        if (ingredient.isEmpty()) {
            return;
        }
        final ResourceKey resource = ingredient.get(currentCycle % ingredient.size());
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        rendering.render(resource, graphics, slotX + 1, slotY + 1);
        ResourceSlotRendering.renderAmount(graphics, slotX + 1, slotY + 1, ingredient.getAmount(), rendering);
    }

    private void renderMatrixSlot(
        final GuiGraphics graphics,
        final int slotX,
        final int slotY,
        final List<ResourceAmount> possibilities
    ) {
        graphics.blitSprite(SLOT, slotX, slotY, 18, 18);
        if (possibilities.isEmpty()) {
            return;
        }
        final ResourceAmount resourceAmount = possibilities.get(currentCycle % possibilities.size());
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            resourceAmount.resource().getClass()
        );
        rendering.render(resourceAmount.resource(), graphics, slotX + 1, slotY + 1);
        ResourceSlotRendering.renderAmount(graphics, slotX + 1, slotY + 1, resourceAmount.amount(), rendering);
    }

    private void renderOutputText(final Font font, final int x, final int y, final GuiGraphics graphics) {
        for (int i = 0; i < outputTexts.size(); ++i) {
            graphics.drawString(
                font,
                outputTexts.get(i),
                x,
                y + (i * 9),
                0xAAAAAA
            );
        }
    }
}
