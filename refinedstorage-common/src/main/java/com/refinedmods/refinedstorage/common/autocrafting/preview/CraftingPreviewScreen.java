package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.amount.AbstractAmountScreen;
import com.refinedmods.refinedstorage.common.support.amount.AmountScreenConfiguration;
import com.refinedmods.refinedstorage.common.support.amount.DoubleAmountOperations;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class CraftingPreviewScreen extends AbstractAmountScreen<CraftingPreviewContainerMenu, Double> {
    static final int REQUEST_BUTTON_HEIGHT = 96 / 4;
    static final int REQUEST_BUTTON_WIDTH = 64;

    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/crafting_preview.png");
    private static final MutableComponent TITLE = Component.translatable("container.crafting");
    private static final MutableComponent START = createTranslation("gui", "crafting_preview.start");
    private static final MutableComponent MISSING_RESOURCES
        = createTranslation("gui", "crafting_preview.start.missing_resources");
    private static final ResourceLocation ROW = createIdentifier("crafting_preview/row");
    private static final ResourceLocation CRAFTING_REQUESTS = createIdentifier("crafting_preview/crafting_requests");

    private static final int ROWS_VISIBLE = 4;
    private static final int COLUMNS = 3;
    private static final int PREVIEW_AREA_HEIGHT = 119;

    private static final int ROW_HEIGHT = 30;
    private static final int ROW_WIDTH = 221;

    private static final int REQUESTS_WIDTH = 91;
    private static final int REQUESTS_HEIGHT = 111;
    private static final int REQUESTS_INNER_WIDTH = 64;
    private static final int REQUESTS_INNER_HEIGHT = 96;
    private static final int REQUESTS_VISIBLE = 4;

    @Nullable
    private ScrollbarWidget previewItemsScrollbar;
    @Nullable
    private ScrollbarWidget requestButtonsScrollbar;

    private final List<CraftingRequestButton> requestButtons = new ArrayList<>();
    private final boolean requestsButtonsVisible;

    public CraftingPreviewScreen(final Screen parent,
                                 final Inventory playerInventory,
                                 final List<CraftingRequest> requests) {
        super(
            new CraftingPreviewContainerMenu(requests),
            parent,
            playerInventory,
            TITLE,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Double>create()
                .withInitialAmount(1D)
                .withIncrementsTop(1, 10, 64)
                .withIncrementsTopStartPosition(new Vector3f(80, 20, 0))
                .withIncrementsBottom(-1, -10, -64)
                .withIncrementsBottomStartPosition(new Vector3f(80, 71, 0))
                .withAmountFieldPosition(new Vector3f(77, 51, 0))
                .withActionButtonsStartPosition(new Vector3f(7, 222, 0))
                .withHorizontalActionButtons(true)
                .withMinAmount(1D)
                .withResetAmount(1D)
                .withConfirmButtonText(START)
                .build(),
            DoubleAmountOperations.INSTANCE
        );
        this.imageWidth = 254;
        this.imageHeight = 249;
        this.requestsButtonsVisible = getMenu().getRequests().size() > 1;
    }

    @Override
    protected void init() {
        super.init();
        previewItemsScrollbar = new ScrollbarWidget(
            leftPos + 235,
            topPos + 98,
            ScrollbarWidget.Type.NORMAL,
            PREVIEW_AREA_HEIGHT
        );
        previewItemsScrollbar.setEnabled(false);
        if (requestsButtonsVisible) {
            initRequestButtons();
        }
        if (confirmButton != null) {
            confirmButton.active = false;
        }
        updateCurrentRequest();
        getExclusionZones().add(new Rect2i(
            leftPos - REQUESTS_WIDTH + 4,
            topPos,
            REQUESTS_WIDTH,
            REQUESTS_HEIGHT
        ));
    }

    private void initRequestButtons() {
        requestButtons.clear();
        requestButtonsScrollbar = new ScrollbarWidget(
            leftPos - 17 + 4,
            getRequestButtonsInnerY(),
            ScrollbarWidget.Type.NORMAL,
            96
        );
        requestButtonsScrollbar.setListener(value -> {
            final int scrollOffset = requestButtonsScrollbar.isSmoothScrolling()
                ? (int) requestButtonsScrollbar.getOffset()
                : (int) requestButtonsScrollbar.getOffset() * REQUEST_BUTTON_HEIGHT;
            for (int i = 0; i < requestButtons.size(); i++) {
                final CraftingRequestButton requestButton = requestButtons.get(i);
                final int y = getCraftingRequestButtonY(i) - scrollOffset;
                requestButton.setY(y);
                requestButton.visible = isCraftingRequestButtonVisible(y);
            }
        });
        final int totalRequestButtons = getMenu().getRequests().size() - REQUESTS_VISIBLE;
        final int maxOffset = requestButtonsScrollbar.isSmoothScrolling()
            ? totalRequestButtons * REQUEST_BUTTON_HEIGHT
            : totalRequestButtons;
        requestButtonsScrollbar.setEnabled(maxOffset > 0);
        requestButtonsScrollbar.setMaxOffset(maxOffset);
        for (int i = 0; i < getMenu().getRequests().size(); ++i) {
            final CraftingRequest request = getMenu().getRequests().get(i);
            final int buttonY = getCraftingRequestButtonY(i);
            final CraftingRequestButton button = new CraftingRequestButton(
                getRequestButtonsInnerX(),
                buttonY,
                request,
                this::changeCurrentRequest
            );
            button.visible = isCraftingRequestButtonVisible(buttonY);
            requestButtons.add(addWidget(button));
        }
    }

    private boolean isCraftingRequestButtonVisible(final int y) {
        final int innerY = getRequestButtonsInnerY();
        return y >= innerY - REQUEST_BUTTON_HEIGHT && y <= innerY + REQUESTS_INNER_HEIGHT;
    }

    private int getCraftingRequestButtonY(final int i) {
        return getRequestButtonsInnerY() + (i * REQUEST_BUTTON_HEIGHT);
    }

    private void changeCurrentRequest(final CraftingRequest request) {
        getMenu().setCurrentRequest(request);
        updateCurrentRequest();
    }

    private void updateCurrentRequest() {
        if (previewItemsScrollbar == null || confirmButton == null) {
            return;
        }
        final CraftingRequest currentRequest = getMenu().getCurrentRequest();
        for (final CraftingRequestButton requestButton : requestButtons) {
            requestButton.active = requestButton.getRequest() != currentRequest;
        }
        updateAmount(currentRequest.getAmount());
        final CraftingPreview preview = currentRequest.getPreview();
        if (preview == null) {
            previewItemsScrollbar.setEnabled(false);
            previewItemsScrollbar.setMaxOffset(0);
            confirmButton.active = false;
            return;
        }
        final int items = preview.items().size();
        final int rows = Math.ceilDiv(items, COLUMNS) - ROWS_VISIBLE;
        previewItemsScrollbar.setMaxOffset(previewItemsScrollbar.isSmoothScrolling() ? rows * ROW_HEIGHT : rows);
        previewItemsScrollbar.setEnabled(rows > 0);
        confirmButton.active = !preview.missing();
        confirmButton.setTooltip(preview.missing() ? Tooltip.create(MISSING_RESOURCES) : null);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (previewItemsScrollbar != null) {
            previewItemsScrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (requestButtonsScrollbar != null) {
            requestButtonsScrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (requestsButtonsVisible) {
            final int requestsInnerX = getRequestButtonsInnerX();
            final int requestsInnerY = getRequestButtonsInnerY();
            graphics.enableScissor(
                requestsInnerX,
                requestsInnerY,
                requestsInnerX + REQUESTS_INNER_WIDTH,
                requestsInnerY + REQUESTS_INNER_HEIGHT
            );
            for (final CraftingRequestButton requestButton : requestButtons) {
                requestButton.render(graphics, mouseX, mouseY, partialTicks);
            }
            graphics.disableScissor();
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        if (requestsButtonsVisible) {
            graphics.blitSprite(CRAFTING_REQUESTS, leftPos - REQUESTS_WIDTH + 4, topPos, REQUESTS_WIDTH,
                REQUESTS_HEIGHT);
        }
        final CraftingRequest request = getMenu().getCurrentRequest();
        final CraftingPreview preview = request.getPreview();
        if (preview == null || previewItemsScrollbar == null) {
            return;
        }
        final int x = leftPos + 8;
        final int y = topPos + 98;
        graphics.enableScissor(x, y, x + 221, y + PREVIEW_AREA_HEIGHT);
        final List<CraftingPreviewItem> items = preview.items();
        final int rows = Math.ceilDiv(items.size(), COLUMNS);
        for (int i = 0; i < rows; ++i) {
            final int scrollOffset = previewItemsScrollbar.isSmoothScrolling()
                ? (int) previewItemsScrollbar.getOffset()
                : (int) previewItemsScrollbar.getOffset() * ROW_HEIGHT;
            final int yy = y + (i * ROW_HEIGHT) - scrollOffset;
            renderRow(graphics, x, yy, i, items, mouseX, mouseY);
        }
        graphics.disableScissor();
    }

    private void renderRow(final GuiGraphics graphics,
                           final int x,
                           final int y,
                           final int i,
                           final List<CraftingPreviewItem> items,
                           final double mouseX,
                           final double mouseY) {
        if (y <= topPos + 98 - ROW_HEIGHT || y > topPos + 98 + PREVIEW_AREA_HEIGHT) {
            return;
        }
        graphics.blitSprite(ROW, x, y, ROW_WIDTH, ROW_HEIGHT);
        for (int column = i * COLUMNS; column < Math.min(i * COLUMNS + COLUMNS, items.size()); ++column) {
            final CraftingPreviewItem item = items.get(column);
            final int xx = x + (column % COLUMNS) * 74;
            renderCell(graphics, xx, y, item, mouseX, mouseY);
        }
    }

    private void renderCell(final GuiGraphics graphics,
                            final int x,
                            final int y,
                            final CraftingPreviewItem item,
                            final double mouseX,
                            final double mouseY) {
        if (item.missing() > 0) {
            graphics.fill(x, y, x + 73, y + 29, 0xFFF2DEDE);
        }
        int xx = x + 2;
        final ResourceRendering rendering = RefinedStorageApi.INSTANCE.getResourceRendering(item.resource().getClass());
        int yy = y + 7;
        rendering.render(item.resource(), graphics, xx, yy);
        if (isHovering(xx - leftPos, yy - topPos, 16, 16, mouseX, mouseY)
            && isHoveringOverPreviewArea(mouseX, mouseY)) {
            setTooltipForNextRenderPass(rendering.getTooltip(item.resource()).stream()
                .map(Component::getVisualOrderText)
                .toList());
        }
        if (!SmallText.isSmall()) {
            yy -= 2;
        }
        xx += 16 + 3;
        if (item.missing() > 0) {
            renderCellText(graphics, "missing", rendering, xx, yy, item.missing());
            yy += 7;
        }
        if (item.available() > 0) {
            renderCellText(graphics, "available", rendering, xx, yy, item.available());
            yy += 7;
        }
        if (item.toCraft() > 0) {
            renderCellText(graphics, "to_craft", rendering, xx, yy, item.toCraft());
        }
    }

    private void renderCellText(final GuiGraphics graphics,
                                final String type,
                                final ResourceRendering rendering,
                                final int x,
                                final int y,
                                final long amount) {
        SmallText.render(
            graphics,
            font,
            createTranslation("gui", "crafting_preview." + type, rendering.formatAmount(amount, true))
                .getVisualOrderText(),
            x,
            y,
            0x404040,
            false
        );
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (previewItemsScrollbar != null && previewItemsScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        if (requestButtonsScrollbar != null && requestButtonsScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (previewItemsScrollbar != null) {
            previewItemsScrollbar.mouseMoved(mx, my);
        }
        if (requestButtonsScrollbar != null) {
            requestButtonsScrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final double mx, final double my, final int button) {
        if (previewItemsScrollbar != null && previewItemsScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        if (requestButtonsScrollbar != null && requestButtonsScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        final boolean didPreviewItemsScrollbar = previewItemsScrollbar != null
            && isHoveringOverPreviewArea(x, y)
            && previewItemsScrollbar.mouseScrolled(x, y, z, delta);
        final boolean didRequestButtonsScrollbar = !didPreviewItemsScrollbar
            && requestButtonsScrollbar != null
            && isHoveringOverRequestButtons(x, y)
            && requestButtonsScrollbar.mouseScrolled(x, y, z, delta);
        return didPreviewItemsScrollbar || didRequestButtonsScrollbar || super.mouseScrolled(x, y, z, delta);
    }

    private boolean isHoveringOverPreviewArea(final double x, final double y) {
        return isHovering(7, 97, 241, 121, x, y);
    }

    private boolean isHoveringOverRequestButtons(final double x, final double y) {
        final int requestsInnerX = getRequestButtonsInnerX() - 1;
        final int requestsInnerY = getRequestButtonsInnerY() - 1;
        return isHovering(requestsInnerX - leftPos, requestsInnerY - topPos, 80, 98, x, y);
    }

    private int getRequestButtonsInnerY() {
        return topPos + 8;
    }

    private int getRequestButtonsInnerX() {
        return leftPos - 83 + 4;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void onAmountFieldChanged() {
        if (amountField == null || confirmButton == null) {
            return;
        }
        confirmButton.active = false;
        final boolean valid = getAndValidateAmount().isPresent();
        amountField.setTextColor(valid ? 0xFFFFFF : 0xFF5555);
    }

    @Override
    protected void reset() {
        updateAmount(getMenu().getCurrentRequest().getAmount());
    }

    @Override
    protected boolean confirm(final Double amount) {
        return false;
    }
}
