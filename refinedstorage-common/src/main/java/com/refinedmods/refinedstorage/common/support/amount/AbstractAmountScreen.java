package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.AlternativesScreen;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.support.Sprites.ICON_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractAmountScreen<T extends AbstractContainerMenu, N extends Number>
    extends AbstractBaseScreen<T> {
    private static final MutableComponent RESET_TEXT = createTranslation("gui", "configure_amount.reset");
    private static final MutableComponent CANCEL_TEXT = Component.translatable("gui.cancel");

    private static final int INCREMENT_BUTTON_WIDTH = 30;
    private static final int ACTION_BUTTON_HEIGHT = 20;
    private static final int ACTION_BUTTON_WIDTH = 58;
    private static final int ACTION_BUTTON_SPACING = 20;

    @Nullable
    protected IconButton confirmButton;
    @Nullable
    protected EditBox amountField;

    @Nullable
    private final Screen parent;
    private final AmountScreenConfiguration<N> configuration;
    private final AmountOperations<N> amountOperations;

    protected AbstractAmountScreen(final T containerMenu,
                                   @Nullable final Screen parent,
                                   final Inventory playerInventory,
                                   final Component title,
                                   final AmountScreenConfiguration<N> configuration,
                                   final AmountOperations<N> amountOperations) {
        super(containerMenu, playerInventory, title);
        this.parent = parent;
        this.configuration = configuration;
        this.amountOperations = amountOperations;
    }

    @Override
    protected void init() {
        super.init();
        if (configuration.isActionButtonsEnabled()) {
            addActionButtons();
        }
        addAmountField();
        addIncrementButtons();
    }

    private void addActionButtons() {
        final Vector3f pos = configuration.getActionButtonsStartPosition();
        if (configuration.isHorizontalActionButtons()) {
            final int spacing = 3;
            final Button cancelButton = addCancelButton((int) pos.x, (int) pos.y);
            final Button resetButton = addResetButton((int) pos.x + cancelButton.getWidth() + spacing, (int) pos.y);
            addConfirmButton((int) pos.x + cancelButton.getWidth() + spacing + resetButton.getWidth() + spacing,
                (int) pos.y);
        } else {
            final int spacing = 24;
            addResetButton((int) pos.x, (int) pos.y);
            addConfirmButton((int) pos.x, (int) pos.y + spacing);
            addCancelButton((int) pos.x, (int) pos.y + spacing * 2);
        }
    }

    private Button addResetButton(final int x, final int y) {
        final int width = configuration.isHorizontalActionButtons()
            ? font.width(RESET_TEXT) + ACTION_BUTTON_SPACING + ICON_SIZE
            : ACTION_BUTTON_WIDTH;
        final IconButton button = new IconButton(
            leftPos + x,
            topPos + y,
            width,
            ACTION_BUTTON_HEIGHT,
            RESET_TEXT,
            btn -> reset()
        );
        button.setIcon(IconButton.Icon.RESET);
        return addRenderableWidget(button);
    }

    private void addConfirmButton(final int x, final int y) {
        final int width = configuration.isHorizontalActionButtons()
            ? font.width(configuration.getConfirmButtonText()) + ACTION_BUTTON_SPACING + ICON_SIZE
            : ACTION_BUTTON_WIDTH;
        final IconButton button = new IconButton(
            leftPos + x,
            topPos + y,
            width,
            ACTION_BUTTON_HEIGHT,
            configuration.getConfirmButtonText(),
            btn -> tryConfirmAndCloseToParent()
        );
        button.setIcon(getConfirmButtonIcon());
        confirmButton = addRenderableWidget(button);
    }

    @Nullable
    protected IconButton.Icon getConfirmButtonIcon() {
        return IconButton.Icon.SET;
    }

    private Button addCancelButton(final int x, final int y) {
        final int width = configuration.isHorizontalActionButtons()
            ? font.width(CANCEL_TEXT) + ACTION_BUTTON_SPACING + ICON_SIZE
            : ACTION_BUTTON_WIDTH;
        final IconButton button = new IconButton(
            leftPos + x,
            topPos + y,
            width,
            ACTION_BUTTON_HEIGHT,
            CANCEL_TEXT,
            btn -> close()
        );
        button.setIcon(IconButton.Icon.CANCEL);
        return addRenderableWidget(button);
    }

    private void addAmountField() {
        final Vector3f pos = configuration.getAmountFieldPosition();
        final String originalValue = amountField != null ? amountField.getValue() : null;
        amountField = new EditBox(
            font,
            leftPos + (int) pos.x(),
            topPos + (int) pos.y(),
            configuration.getAmountFieldWidth() - 6,
            font.lineHeight,
            Component.empty()
        );
        amountField.setBordered(false);
        amountField.setTextColor(0xFFFFFF);
        if (originalValue != null) {
            amountField.setValue(originalValue);
            onAmountFieldChanged();
        } else if (configuration.getInitialAmount() != null) {
            updateAmount(configuration.getInitialAmount());
        }
        amountField.setVisible(true);
        amountField.setCanLoseFocus(this instanceof AlternativesScreen);
        amountField.setFocused(true);
        amountField.setResponder(value -> onAmountFieldChanged());
        setFocused(amountField);

        addRenderableWidget(amountField);
    }

    protected final void updateAmount(final N amount) {
        if (amountField == null) {
            return;
        }
        amountField.setValue(amountOperations.format(amount));
    }

    protected void onAmountFieldChanged() {
        if (amountField == null) {
            return;
        }
        final boolean valid = getAndValidateAmount().isPresent();
        if (confirmButton != null) {
            confirmButton.active = valid;
            confirmButton.setIcon(valid ? getConfirmButtonIcon() : IconButton.Icon.ERROR);
        } else {
            tryConfirm();
        }
        amountField.setTextColor(valid ? 0xFFFFFF : 0xFF5555);
    }

    private void addIncrementButtons() {
        final Vector3f incrementsTopPos = configuration.getIncrementsTopStartPosition();
        addIncrementButtons(
            configuration.getIncrementsTop(),
            leftPos + (int) incrementsTopPos.x,
            topPos + (int) incrementsTopPos.y
        );
        final Vector3f incrementsBottomPos = configuration.getIncrementsBottomStartPosition();
        addIncrementButtons(
            configuration.getIncrementsBottom(),
            leftPos + (int) incrementsBottomPos.x,
            topPos + (int) incrementsBottomPos.y
        );
    }

    private void addIncrementButtons(final int[] increments, final int x, final int y) {
        for (int i = 0; i < increments.length; ++i) {
            final int increment = increments[i];
            final int xx = x + ((INCREMENT_BUTTON_WIDTH + 3) * i);
            addRenderableWidget(createIncrementButton(xx, y, increment));
        }
    }

    protected abstract boolean confirm(N amount);

    private Button createIncrementButton(final int x, final int y, final int increment) {
        final Component text = Component.literal((increment > 0 ? "+" : "") + increment);
        return Button.builder(text, btn -> changeAmount(increment))
            .pos(x, y)
            .size(INCREMENT_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
            .build();
    }

    private void changeAmount(final int delta) {
        if (amountField == null) {
            return;
        }
        getAndValidateAmount().ifPresent(oldAmount -> {
            final int correctedDelta = correctDelta(oldAmount, delta);
            final N newAmount = amountOperations.changeAmount(
                oldAmount,
                correctedDelta,
                configuration.getMinAmount(),
                configuration.getMaxAmount()
            );
            updateAmount(newAmount);
        });
    }

    private int correctDelta(final N oldAmount, final int delta) {
        // if we do +10, and the current value is 1, we want to end up with 10, not 11
        // if we do +1, and the current value is 1, we want to end up with 2
        if (oldAmount.intValue() == 1 && delta > 1) {
            return delta - 1;
        }
        return delta;
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        if (delta > 0) {
            changeAmount(1);
        } else {
            changeAmount(-1);
        }
        return super.mouseScrolled(x, y, z, delta);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (amountField != null && amountField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (tryClose(key)) {
            return true;
        }
        if (amountField != null
            && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)
            && amountField.isFocused()) {
            tryConfirmAndCloseToParent();
            return true;
        }
        if (amountField != null
            && (amountField.keyPressed(key, scanCode, modifiers) || amountField.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    protected final boolean tryClose(final int key) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    protected void reset() {
        if (amountField == null || configuration.getResetAmount() == null) {
            return;
        }
        updateAmount(configuration.getResetAmount());
    }

    private void tryConfirm() {
        getAndValidateAmount().ifPresent(this::confirm);
    }

    private void tryConfirmAndCloseToParent() {
        getAndValidateAmount().ifPresent(value -> {
            if (confirm(value)) {
                tryCloseToParent();
            }
        });
    }

    private boolean tryCloseToParent() {
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return false;
    }

    protected final void close() {
        if (!tryCloseToParent()) {
            onClose();
        }
    }

    protected final Optional<N> getAndValidateAmount() {
        if (amountField == null) {
            return Optional.empty();
        }
        return amountOperations.parse(amountField.getValue()).flatMap(amount -> amountOperations.validate(
            amount,
            configuration.getMinAmount(),
            configuration.getMaxAmount()
        ));
    }

    protected static class DefaultDummyContainerMenu extends AbstractContainerMenu {
        protected DefaultDummyContainerMenu() {
            super(null, 0);
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(final Player player) {
            return true;
        }
    }
}
