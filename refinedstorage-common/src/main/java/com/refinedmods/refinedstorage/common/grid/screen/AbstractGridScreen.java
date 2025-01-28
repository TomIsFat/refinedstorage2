package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.grid.view.GridView;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.view.PlatformGridResource;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.AutocraftableResourceHint;
import com.refinedmods.refinedstorage.common.grid.NoopGridSynchronizer;
import com.refinedmods.refinedstorage.common.grid.view.ItemGridResource;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.containermenu.DisabledSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallTextClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.widget.AutoSelectedSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchIconWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlighter;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlighterColors;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationKey;

public abstract class AbstractGridScreen<T extends AbstractGridContainerMenu> extends AbstractStretchingScreen<T> {
    protected static final int CLEAR_BUTTON_SIZE = 7;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGridScreen.class);

    private static final ResourceLocation ROW_SPRITE = createIdentifier("grid/row");
    private static final int MODIFIED_JUST_NOW_MAX_SECONDS = 10;
    private static final int COLUMNS = 9;
    private static final int DISABLED_SLOT_COLOR = 0xFF5B5B5B;
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();
    private static final Component SEARCH_HELP = createTranslation("gui", "grid.search_help")
        .append("\n")
        .append(createTranslation("gui", "grid.search_help.mod_search").withStyle(ChatFormatting.GRAY))
        .append("\n")
        .append(createTranslation("gui", "grid.search_help.tag_search").withStyle(ChatFormatting.GRAY))
        .append("\n")
        .append(createTranslation("gui", "grid.search_help.tooltip_search").withStyle(ChatFormatting.GRAY));

    protected final int bottomHeight;

    @Nullable
    GridSearchBoxWidget searchField;

    private int totalRows;
    private int currentGridSlotIndex;

    protected AbstractGridScreen(final T menu,
                                 final Inventory playerInventory,
                                 final Component title,
                                 final int bottomHeight) {
        super(menu, playerInventory, new TextMarquee(title, 70));
        this.bottomHeight = bottomHeight;
    }

    @Override
    protected void init(final int rows) {
        LOGGER.debug("Initializing grid screen - this shouldn't happen too much!");

        if (searchField == null) {
            searchField = new GridSearchBoxWidget(
                font,
                leftPos + 94 + 1,
                topPos + 6 + 1,
                73 - 6,
                new SyntaxHighlighter(SyntaxHighlighterColors.DEFAULT_COLORS),
                new History(SEARCH_FIELD_HISTORY)
            );
        } else {
            searchField.setX(leftPos + 94 + 1);
            searchField.setY(topPos + 6 + 1);
        }
        getMenu().setSearchBox(searchField);

        getMenu().getView().setListener(this::updateScrollbar);
        updateScrollbar();

        addWidget(searchField);

        if (getMenu().hasProperty(PropertyTypes.REDSTONE_MODE)) {
            addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        }
        addSideButton(new ViewTypeSideButtonWidget(getMenu()));
        addSideButton(new ResourceTypeSideButtonWidget(getMenu()));
        addSideButton(new SortingDirectionSideButtonWidget(getMenu()));
        addSideButton(new SortingTypeSideButtonWidget(getMenu()));
        addSideButton(new AutoSelectedSideButtonWidget(searchField));

        addRenderableWidget(new SearchIconWidget(
            leftPos + 79,
            topPos + 5,
            () -> SEARCH_HELP,
            searchField
        ));

        final boolean onlyHasNoopSynchronizer = RefinedStorageApi.INSTANCE.getGridSynchronizerRegistry()
            .getAll()
            .stream()
            .allMatch(synchronizer -> synchronizer == NoopGridSynchronizer.INSTANCE);
        if (!onlyHasNoopSynchronizer) {
            addSideButton(new SynchronizationModeSideButtonWidget(getMenu()));
            searchField.addListener(this::trySynchronizeFromGrid);
        }
    }

    private void trySynchronizeFromGrid(final String text) {
        getMenu().getSynchronizer().synchronizeFromGrid(text);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        trySynchronizeToGrid();
    }

    private void trySynchronizeToGrid() {
        if (searchField == null) {
            return;
        }
        final String text = getMenu().getSynchronizer().getTextToSynchronizeToGrid();
        if (text == null || searchField.getValue().equals(text)) {
            return;
        }
        searchField.setValue(text);
    }

    private void updateScrollbar() {
        this.totalRows = (int) Math.ceil((float) getMenu().getView().getViewList().size() / (float) COLUMNS);
        updateScrollbar(totalRows);
    }

    private boolean isOverStorageArea(final int mouseX, final int mouseY) {
        final int relativeMouseX = mouseX - leftPos;
        final int relativeMouseY = mouseY - topPos;
        return relativeMouseX >= 7
            && relativeMouseX <= 168
            && isInStretchedArea(relativeMouseY);
    }

    @Override
    protected void renderStretchingBackground(final GuiGraphics graphics, final int x, final int y, final int rows) {
        for (int row = 0; row < rows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == rows - 1) {
                textureY = 55;
            }
            graphics.blit(getTexture(), x, y + (ROW_SIZE * row), 0, textureY, imageWidth, ROW_SIZE);
        }
    }

    @Override
    protected int getBottomHeight() {
        return bottomHeight;
    }

    @Override
    protected int getBottomV() {
        return 73;
    }

    @Override
    protected void renderRows(final GuiGraphics graphics,
                              final int x,
                              final int y,
                              final int topHeight,
                              final int rows,
                              final int mouseX,
                              final int mouseY) {
        currentGridSlotIndex = -1;
        for (int row = 0; row < Math.max(totalRows, rows); ++row) {
            final int rowX = x + 7;
            final int rowY = y + topHeight + (row * ROW_SIZE) - getScrollbarOffset();
            final boolean isOutOfFrame = (rowY < y + topHeight - ROW_SIZE)
                || (rowY > y + topHeight + (ROW_SIZE * rows));
            if (isOutOfFrame) {
                continue;
            }
            renderRow(graphics, mouseX, mouseY, rowX, rowY, row);
        }
    }

    private void renderRow(final GuiGraphics graphics,
                           final int mouseX,
                           final int mouseY,
                           final int rowX,
                           final int rowY,
                           final int row) {
        graphics.blitSprite(ROW_SPRITE, rowX, rowY, 162, ROW_SIZE);
        for (int column = 0; column < COLUMNS; ++column) {
            renderCell(graphics, mouseX, mouseY, rowX, rowY, (row * COLUMNS) + column, column);
        }
    }

    private void renderCell(final GuiGraphics graphics,
                            final int mouseX,
                            final int mouseY,
                            final int rowX,
                            final int rowY,
                            final int idx,
                            final int column) {
        final GridView view = getMenu().getView();
        final int slotX = rowX + 1 + (column * ROW_SIZE);
        final int slotY = rowY + 1;
        if (!getMenu().isActive()) {
            renderDisabledSlot(graphics, slotX, slotY);
        } else {
            renderSlot(graphics, mouseX, mouseY, idx, view, slotX, slotY);
        }
    }

    @Override
    protected List<ClientTooltipComponent> getResourceSlotTooltip(final ResourceKey resource, final ResourceSlot slot) {
        final List<ClientTooltipComponent> tooltip = super.getResourceSlotTooltip(resource, slot);
        final AutocraftableResourceHint autocraftableHint = getMenu().getAutocraftableResourceHint(slot);
        if (autocraftableHint != null) {
            tooltip.add(AutocraftableClientTooltipComponent.autocraftable(autocraftableHint));
        }
        return tooltip;
    }

    @Override
    protected void renderSlot(final GuiGraphics guiGraphics, final Slot slot) {
        tryRenderAutocraftableResourceHintBackground(guiGraphics, slot);
        super.renderSlot(guiGraphics, slot);
    }

    private void renderSlot(final GuiGraphics graphics,
                            final int mouseX,
                            final int mouseY,
                            final int idx,
                            final GridView view,
                            final int slotX,
                            final int slotY) {
        final boolean inBounds = mouseX >= slotX
            && mouseY >= slotY
            && mouseX <= slotX + 16
            && mouseY <= slotY + 16;
        GridResource resource = null;
        if (idx < view.getViewList().size()) {
            resource = view.getViewList().get(idx);
            renderResourceWithAmount(graphics, slotX, slotY, resource);
        }
        if (inBounds && isOverStorageArea(mouseX, mouseY)) {
            AbstractContainerScreen.renderSlotHighlight(graphics, slotX, slotY, 0);
            if (resource != null) {
                currentGridSlotIndex = idx;
            }
        }
    }

    private void tryRenderAutocraftableResourceHintBackground(final GuiGraphics graphics, final Slot slot) {
        if (!slot.isHighlightable() || !slot.isActive()) {
            return;
        }
        final AutocraftableResourceHint hint = getMenu().getAutocraftableResourceHint(slot);
        if (hint != null) {
            renderSlotBackground(graphics, slot.x, slot.y, getMenu().isLargeSlot(slot), hint.getColor());
        }
    }

    private void renderResourceWithAmount(final GuiGraphics graphics,
                                          final int slotX,
                                          final int slotY,
                                          final GridResource resource) {
        if (resource.isAutocraftable()) {
            renderSlotBackground(
                graphics,
                slotX,
                slotY,
                false,
                AutocraftableResourceHint.AUTOCRAFTABLE.getColor()
            );
        } else if (resource.getAmount(getMenu().getView()) == 0) {
            renderSlotBackground(
                graphics,
                slotX,
                slotY,
                false,
                0x66FF0000
            );
        }
        if (resource instanceof PlatformGridResource platformResource) {
            platformResource.render(graphics, slotX, slotY);
        }
        renderAmount(graphics, slotX, slotY, resource);
    }

    public static void renderSlotBackground(final GuiGraphics graphics,
                                            final int slotX,
                                            final int slotY,
                                            final boolean large,
                                            final int color) {
        final int offset = large ? 4 : 0;
        graphics.fill(
            slotX - offset,
            slotY - offset,
            slotX + 16 + offset,
            slotY + 16 + offset,
            color
        );
    }

    private void renderAmount(final GuiGraphics graphics,
                              final int slotX,
                              final int slotY,
                              final GridResource resource) {
        if (!(resource instanceof PlatformGridResource platformResource)) {
            return;
        }
        final long amount = platformResource.getAmount(getMenu().getView());
        final String text = getAmountText(resource, platformResource, amount);
        final int color = getAmountColor(resource, amount);
        final boolean large = (minecraft != null && minecraft.isEnforceUnicode())
            || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
        ResourceSlotRendering.renderAmount(graphics, slotX, slotY, text, color, large);
    }

    private int getAmountColor(final GridResource resource, final long amount) {
        if (amount == 0) {
            if (resource.isAutocraftable()) {
                return 0xFFFFFF;
            }
            return 0xFF5555;
        }
        return 0xFFFFFF;
    }

    private String getAmountText(final GridResource resource,
                                 final PlatformGridResource platformResource,
                                 final long amount) {
        if (amount == 0 && resource.isAutocraftable()) {
            return I18n.get(createTranslationKey("gui", "grid.craft"));
        }
        return platformResource.getDisplayedAmount(getMenu().getView());
    }

    private void renderDisabledSlot(final GuiGraphics graphics, final int slotX, final int slotY) {
        graphics.fill(RenderType.guiOverlay(), slotX, slotY, slotX + 16, slotY + 16, DISABLED_SLOT_COLOR);
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (isOverStorageArea(x, y)) {
            renderOverStorageAreaTooltip(graphics, x, y);
            return;
        }
        if (getMenu().getCarried().isEmpty() && tryRenderAutocraftableResourceHintTooltip(graphics, x, y)) {
            return;
        }
        super.renderTooltip(graphics, x, y);
    }

    private boolean tryRenderAutocraftableResourceHintTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return false;
        }
        final AutocraftableResourceHint hint = getMenu().getAutocraftableResourceHint(hoveredSlot);
        if (hint == null) {
            return false;
        }
        final ItemStack stack = hoveredSlot.getItem();
        final List<Component> lines = getTooltipFromContainerItem(stack);
        final List<ClientTooltipComponent> processedLines = Platform.INSTANCE.processTooltipComponents(
            stack,
            graphics,
            x,
            stack.getTooltipImage(),
            lines
        );
        processedLines.add(AutocraftableClientTooltipComponent.autocraftable(hint));
        Platform.INSTANCE.renderTooltip(graphics, processedLines, x, y);
        return true;
    }

    private void renderOverStorageAreaTooltip(final GuiGraphics graphics, final int x, final int y) {
        final PlatformGridResource gridResource = getCurrentGridResource();
        if (gridResource != null) {
            renderHoveredResourceTooltip(graphics, x, y, menu.getView(), gridResource);
            return;
        }
        final ItemStack carried = getMenu().getCarried();
        if (carried.isEmpty()) {
            return;
        }
        final List<ClientTooltipComponent> hints = RefinedStorageClientApi.INSTANCE.getGridInsertionHints()
            .getHints(carried);
        Platform.INSTANCE.renderTooltip(graphics, hints, x, y);
    }

    private void renderHoveredResourceTooltip(final GuiGraphics graphics,
                                              final int mouseX,
                                              final int mouseY,
                                              final GridView view,
                                              final PlatformGridResource gridResource) {
        final ItemStack stackContext = gridResource instanceof ItemGridResource itemGridResource
            ? itemGridResource.getItemStack()
            : ItemStack.EMPTY;
        final List<Component> lines = gridResource.getTooltip();
        final List<ClientTooltipComponent> processedLines = Platform.INSTANCE.processTooltipComponents(
            stackContext,
            graphics,
            mouseX,
            gridResource.getTooltipImage(),
            lines
        );
        final long amount = gridResource.getAmount(getMenu().getView());
        if (amount > 0 && Platform.INSTANCE.getConfig().getGrid().isDetailedTooltip()) {
            addDetailedTooltip(view, gridResource, processedLines);
        }
        if (gridResource.isAutocraftable()) {
            processedLines.add(amount == 0
                ? AutocraftableClientTooltipComponent.empty()
                : AutocraftableClientTooltipComponent.existing());
        }
        if (amount > 0) {
            processedLines.addAll(gridResource.getExtractionHints(getMenu().getCarried(), getMenu().getView()));
        }
        Platform.INSTANCE.renderTooltip(graphics, processedLines, mouseX, mouseY);
    }

    private void addDetailedTooltip(final GridView view,
                                    final PlatformGridResource platformResource,
                                    final List<ClientTooltipComponent> lines) {
        final String amountInTooltip = platformResource.getAmountInTooltip(getMenu().getView());
        lines.add(new SmallTextClientTooltipComponent(
            createTranslation("misc", "total", amountInTooltip).withStyle(ChatFormatting.GRAY)
        ));
        platformResource.getTrackedResource(view).ifPresent(entry -> lines.add(new SmallTextClientTooltipComponent(
            getLastModifiedText(entry).withStyle(ChatFormatting.GRAY)
        )));
    }

    private MutableComponent getLastModifiedText(final TrackedResource trackedResource) {
        final LastModified lastModified = LastModified.calculate(trackedResource.getTime(), System.currentTimeMillis());
        if (isModifiedJustNow(lastModified)) {
            return createTranslation("misc", "last_modified.just_now", trackedResource.getSourceName());
        }

        String translationKey = lastModified.type().toString().toLowerCase();
        final boolean plural = lastModified.amount() != 1;
        if (plural) {
            translationKey += "s";
        }

        return createTranslation(
            "misc",
            "last_modified." + translationKey,
            lastModified.amount(),
            trackedResource.getSourceName()
        );
    }

    private boolean isModifiedJustNow(final LastModified lastModified) {
        return lastModified.type() == LastModified.Type.SECOND
            && lastModified.amount() <= MODIFIED_JUST_NOW_MAX_SECONDS;
    }

    @API(status = API.Status.INTERNAL)
    @Nullable
    public PlatformGridResource getCurrentGridResource() {
        if (currentGridSlotIndex < 0) {
            return null;
        }
        final List<GridResource> viewList = menu.getView().getViewList();
        if (currentGridSlotIndex >= viewList.size()) {
            return null;
        }
        return (PlatformGridResource) viewList.get(currentGridSlotIndex);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (searchField != null) {
            searchField.render(graphics, 0, 0, 0);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        final ItemStack carriedStack = getMenu().getCarried();
        final PlatformGridResource resource = getCurrentGridResource();

        if (resource != null) {
            if (resource.canExtract(carriedStack, getMenu().getView()) && !hasControlDown()) {
                mouseClickedInGrid(clickedButton, resource);
                return true;
            } else if (resource.isAutocraftable() && tryStartAutocrafting(resource)) {
                return true;
            }
        }

        if (isOverStorageArea((int) mouseX, (int) mouseY)
            && !carriedStack.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
            mouseClickedInGrid(clickedButton);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    private boolean tryStartAutocrafting(final PlatformGridResource resource) {
        final ResourceAmount request = resource.getAutocraftingRequest();
        if (request == null) {
            return false;
        }
        RefinedStorageClientApi.INSTANCE.openAutocraftingPreview(List.of(request), this);
        return true;
    }

    private void mouseClickedInGrid(final int clickedButton) {
        final GridInsertMode mode = clickedButton == 1
            ? GridInsertMode.SINGLE_RESOURCE
            : GridInsertMode.ENTIRE_RESOURCE;
        final boolean tryAlternatives = clickedButton == 1;
        getMenu().onInsert(mode, tryAlternatives);
    }

    protected void mouseClickedInGrid(final int clickedButton, final PlatformGridResource resource) {
        resource.onExtract(
            getExtractMode(clickedButton),
            shouldExtractToCursor(),
            getMenu()
        );
    }

    private static GridExtractMode getExtractMode(final int clickedButton) {
        if (clickedButton == 1) {
            return GridExtractMode.HALF_RESOURCE;
        }
        return GridExtractMode.ENTIRE_RESOURCE;
    }

    private static boolean shouldExtractToCursor() {
        return !Screen.hasShiftDown();
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        final boolean up = delta > 0;

        if (isOverStorageArea((int) x, (int) y)) {
            final PlatformGridResource resource = getCurrentGridResource();
            if (resource != null) {
                mouseScrolledInGrid(up, resource);
            }
        } else if (hoveredSlot != null && hoveredSlot.hasItem() && !(hoveredSlot instanceof DisabledSlot)) {
            mouseScrolledInInventory(up, hoveredSlot);
        }

        return super.mouseScrolled(x, y, z, delta);
    }

    private void mouseScrolledInInventory(final boolean up, final Slot slot) {
        getMenu().getView().setPreventSorting(true);
        final int slotIndex = slot.getContainerSlot();
        mouseScrolledInInventory(up, slot.getItem(), slotIndex);
    }

    private void mouseScrolledInInventory(final boolean up, final ItemStack stack, final int slotIndex) {
        final GridScrollMode scrollMode = getScrollModeWhenScrollingOnInventoryArea(up);
        if (scrollMode == null) {
            return;
        }
        getMenu().onScroll(ItemResource.ofItemStack(stack), scrollMode, slotIndex);
    }

    private void mouseScrolledInGrid(final boolean up, final PlatformGridResource resource) {
        getMenu().getView().setPreventSorting(true);
        final GridScrollMode scrollMode = getScrollModeWhenScrollingOnGridArea(up);
        if (scrollMode == null) {
            return;
        }
        resource.onScroll(scrollMode, getMenu());
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScrollingOnInventoryArea(final boolean up) {
        if (Screen.hasShiftDown()) {
            return up ? GridScrollMode.INVENTORY_TO_GRID : GridScrollMode.GRID_TO_INVENTORY;
        }
        return null;
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScrollingOnGridArea(final boolean up) {
        final boolean shift = Screen.hasShiftDown();
        final boolean ctrl = Screen.hasControlDown();
        if (shift && ctrl) {
            return null;
        }
        return getScrollModeWhenScrollingOnGridArea(up, shift, ctrl);
    }

    @Nullable
    private static GridScrollMode getScrollModeWhenScrollingOnGridArea(final boolean up,
                                                                       final boolean shift,
                                                                       final boolean ctrl) {
        if (up) {
            if (shift) {
                return GridScrollMode.INVENTORY_TO_GRID;
            }
        } else {
            if (shift) {
                return GridScrollMode.GRID_TO_INVENTORY;
            } else if (ctrl) {
                return GridScrollMode.GRID_TO_CURSOR;
            }
        }
        return null;
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (searchField != null && searchField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        // First check if we have to prevent sorting.
        // Order matters. In auto-selected mode, the search field will swallow the SHIFT key.
        if (Screen.hasShiftDown() && Platform.INSTANCE.getConfig().getGrid().isPreventSortingWhileShiftIsDown()) {
            getMenu().getView().setPreventSorting(true);
        }

        if (searchField != null && searchField.keyPressed(key, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(final int key, final int scanCode, final int modifiers) {
        if (getMenu().getView().setPreventSorting(false)) {
            getMenu().getView().sort();
        }

        return super.keyReleased(key, scanCode, modifiers);
    }
}
