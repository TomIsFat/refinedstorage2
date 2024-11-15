package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.support.Sprites.ERROR;
import static com.refinedmods.refinedstorage.common.support.Sprites.ERROR_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class AutocraftingMonitorScreen extends AbstractBaseScreen<AutocraftingMonitorContainerMenu>
    implements AutocraftingMonitorListener {
    static final int TASK_BUTTON_HEIGHT = 168 / 7;
    static final int TASK_BUTTON_WIDTH = 64;

    private static final int ROWS_VISIBLE = 6;
    private static final int COLUMNS = 3;
    private static final int ITEMS_AREA_HEIGHT = 179;

    private static final int PROCESSING_COLOR = 0xFFD9EDF7;
    private static final int MISSING_COLOR = 0xFFF2DEDE;
    private static final int SCHEDULED_COLOR = 0xFFE8E5CA;
    private static final int CRAFTING_COLOR = 0xFFADDBC6;

    private static final int ROW_HEIGHT = 30;
    private static final int ROW_WIDTH = 221;

    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/autocrafting_monitor.png");
    private static final ResourceLocation ROW = createIdentifier("autocrafting_monitor/row");
    private static final ResourceLocation TASKS = createIdentifier("autocrafting_monitor/tasks");

    private static final MutableComponent MACHINE_DOES_NOT_ACCEPT_RESOURCE = createTranslation(
        "gui",
        "autocrafting_monitor.machine_does_not_accept_resource"
    ).withStyle(ChatFormatting.RED);
    private static final MutableComponent NO_MACHINE_FOUND = createTranslation(
        "gui",
        "autocrafting_monitor.no_machine_found"
    ).withStyle(ChatFormatting.RED);
    private static final MutableComponent AUTOCRAFTER_IS_LOCKED = createTranslation(
        "gui",
        "autocrafting_monitor.autocrafter_is_locked"
    ).withStyle(ChatFormatting.RED);

    private static final MutableComponent CANCEL = createTranslation("gui", "autocrafting_monitor.cancel");
    private static final MutableComponent CANCEL_ALL = createTranslation("gui", "autocrafting_monitor.cancel_all");

    private static final int TASKS_WIDTH = 91;
    private static final int TASKS_HEIGHT = 183;
    private static final int TASKS_INNER_WIDTH = 64;
    private static final int TASKS_INNER_HEIGHT = 168;
    private static final int TASKS_VISIBLE = 7;

    @Nullable
    private ScrollbarWidget taskItemsScrollbar;
    @Nullable
    private ScrollbarWidget taskButtonsScrollbar;

    @Nullable
    private Button cancelButton;
    @Nullable
    private Button cancelAllButton;

    private final List<AutocraftingTaskButton> taskButtons = new ArrayList<>();

    public AutocraftingMonitorScreen(final AutocraftingMonitorContainerMenu menu,
                                     final Inventory playerInventory,
                                     final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 254;
        this.imageHeight = 231;
    }

    @Override
    protected void init() {
        super.init();
        taskItemsScrollbar = new ScrollbarWidget(
            leftPos + 235,
            topPos + 20,
            ScrollbarWidget.Type.NORMAL,
            ITEMS_AREA_HEIGHT
        );
        taskItemsScrollbar.setEnabled(false);
        initTaskButtons();
        getMenu().setListener(this);
        getExclusionZones().add(new Rect2i(
            leftPos - TASKS_WIDTH + 4,
            topPos,
            TASKS_WIDTH,
            TASKS_HEIGHT
        ));
        final int cancelButtonsY = topPos + 204;
        cancelButton = addRenderableWidget(Button.builder(CANCEL, button -> getMenu().cancelCurrentTask())
            .pos(leftPos + 7, cancelButtonsY)
            .size(font.width(CANCEL) + 14, 20).build());
        cancelButton.active = false;
        cancelAllButton = addRenderableWidget(Button.builder(CANCEL_ALL, button -> getMenu().cancelAllTasks())
            .pos(cancelButton.getX() + cancelButton.getWidth() + 4, cancelButtonsY)
            .size(font.width(CANCEL_ALL) + 14, 20).build());
        cancelAllButton.active = false;
        getMenu().loadCurrentTask();
    }

    private void initTaskButtons() {
        taskButtons.clear();
        taskButtonsScrollbar = new ScrollbarWidget(
            leftPos - 17 + 4,
            getTaskButtonsInnerY(),
            ScrollbarWidget.Type.NORMAL,
            168
        );
        taskButtonsScrollbar.setListener(value -> {
            final int scrollOffset = taskButtonsScrollbar.isSmoothScrolling()
                ? (int) taskButtonsScrollbar.getOffset()
                : (int) taskButtonsScrollbar.getOffset() * TASK_BUTTON_HEIGHT;
            for (int i = 0; i < taskButtons.size(); i++) {
                final AutocraftingTaskButton taskButton = taskButtons.get(i);
                final int y = getTaskButtonY(i) - scrollOffset;
                taskButton.setY(y);
                taskButton.visible = isTaskButtonVisible(y);
            }
        });
        updateTaskButtonsScrollbar();
        for (int i = 0; i < getMenu().getTasks().size(); ++i) {
            final TaskStatus.TaskInfo taskId = getMenu().getTasks().get(i);
            final int buttonY = getTaskButtonY(i);
            final AutocraftingTaskButton button = new AutocraftingTaskButton(
                getTaskButtonsInnerX(),
                buttonY,
                taskId,
                menu::setCurrentTaskId,
                menu::getPercentageCompleted
            );
            button.visible = isTaskButtonVisible(buttonY);
            taskButtons.add(addWidget(button));
        }
    }

    private boolean isTaskButtonVisible(final int y) {
        final int innerY = getTaskButtonsInnerY();
        return y >= innerY - TASK_BUTTON_HEIGHT && y <= innerY + TASKS_INNER_HEIGHT;
    }

    private int getTaskButtonY(final int i) {
        return getTaskButtonsInnerY() + (i * TASK_BUTTON_HEIGHT);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (taskItemsScrollbar != null) {
            taskItemsScrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (taskButtonsScrollbar != null) {
            taskButtonsScrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }

        final int tasksInnerX = getTaskButtonsInnerX();
        final int tasksInnerY = getTaskButtonsInnerY();
        graphics.enableScissor(
            tasksInnerX,
            tasksInnerY,
            tasksInnerX + TASKS_INNER_WIDTH,
            tasksInnerY + TASKS_INNER_HEIGHT
        );
        for (final AutocraftingTaskButton taskButton : taskButtons) {
            taskButton.render(graphics, mouseX, mouseY, partialTicks);
        }
        graphics.disableScissor();
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        graphics.blitSprite(TASKS, leftPos - TASKS_WIDTH + 4, topPos, TASKS_WIDTH, TASKS_HEIGHT);
        final List<TaskStatus.Item> items = getMenu().getCurrentItems();
        if (items.isEmpty() || taskItemsScrollbar == null) {
            return;
        }
        final int x = leftPos + 8;
        final int y = topPos + 20;
        graphics.enableScissor(x, y, x + 221, y + ITEMS_AREA_HEIGHT);
        final int rows = Math.ceilDiv(items.size(), COLUMNS);
        for (int i = 0; i < rows; ++i) {
            final int scrollOffset = taskItemsScrollbar.isSmoothScrolling()
                ? (int) taskItemsScrollbar.getOffset()
                : (int) taskItemsScrollbar.getOffset() * ROW_HEIGHT;
            final int yy = y + (i * ROW_HEIGHT) - scrollOffset;
            renderRow(graphics, x, yy, i, items, mouseX, mouseY);
        }
        graphics.disableScissor();
    }

    private void renderRow(final GuiGraphics graphics,
                           final int x,
                           final int y,
                           final int i,
                           final List<TaskStatus.Item> items,
                           final double mouseX,
                           final double mouseY) {
        if (y <= topPos + 20 - ROW_HEIGHT || y > topPos + 20 + ITEMS_AREA_HEIGHT) {
            return;
        }
        graphics.blitSprite(ROW, x, y, ROW_WIDTH, ROW_HEIGHT);
        for (int column = i * COLUMNS; column < Math.min(i * COLUMNS + COLUMNS, items.size()); ++column) {
            final TaskStatus.Item item = items.get(column);
            final int xx = x + (column % COLUMNS) * 74;
            renderItem(graphics, xx, y, item, mouseX, mouseY);
        }
    }

    private static int getItemColor(final TaskStatus.Item item) {
        if (item.missing() > 0) {
            return MISSING_COLOR;
        }
        return getItemColor2(item);
    }

    private static int getItemColor2(final TaskStatus.Item item) {
        if (item.processing() > 0) {
            return PROCESSING_COLOR;
        }
        if (item.scheduled() > 0) {
            return SCHEDULED_COLOR;
        }
        if (item.crafting() > 0) {
            return CRAFTING_COLOR;
        }
        return 0;
    }

    private void renderItem(final GuiGraphics graphics,
                            final int x,
                            final int y,
                            final TaskStatus.Item item,
                            final double mouseX,
                            final double mouseY) {
        final int color = getItemColor(item);
        if (color != 0) {
            graphics.fill(x, y, x + 73, y + 29, color);
        }
        if (item.type() != TaskStatus.ItemType.NORMAL) {
            renderItemErrorIcon(graphics, x, y);
        }
        int xx = x + 2;
        final ResourceRendering rendering = RefinedStorageApi.INSTANCE.getResourceRendering(item.resource().getClass());
        int yy = y + 7;
        rendering.render(item.resource(), graphics, xx, yy);
        if (isHovering(x - leftPos, y - topPos, 73, 29, mouseX, mouseY)
            && isHoveringOverItems(mouseX, mouseY)) {
            setTooltipForNextRenderPass(getItemTooltip(item, rendering));
        }
        if (!SmallText.isSmall()) {
            yy -= 2;
        }
        xx += 16 + 3;
        renderItemText(graphics, item, rendering, xx, yy);
    }

    private static void renderItemErrorIcon(final GuiGraphics graphics, final int x, final int y) {
        graphics.blitSprite(
            ERROR,
            x + 73 - ERROR_SIZE - 3,
            y + (29 / 2) - (ERROR_SIZE / 2),
            ERROR_SIZE,
            ERROR_SIZE
        );
    }

    private List<FormattedCharSequence> getItemTooltip(final TaskStatus.Item item, final ResourceRendering rendering) {
        final List<FormattedCharSequence> tooltip = rendering.getTooltip(item.resource()).stream()
            .map(Component::getVisualOrderText)
            .collect(Collectors.toList());
        if (item.type() != TaskStatus.ItemType.NORMAL) {
            tooltip.add(getErrorTooltip(item.type()).getVisualOrderText());
        }
        return tooltip;
    }

    private Component getErrorTooltip(final TaskStatus.ItemType type) {
        return switch (type) {
            case MACHINE_DOES_NOT_ACCEPT_RESOURCE -> MACHINE_DOES_NOT_ACCEPT_RESOURCE;
            case NO_MACHINE_FOUND -> NO_MACHINE_FOUND;
            case AUTOCRAFTER_IS_LOCKED -> AUTOCRAFTER_IS_LOCKED;
            default -> Component.empty();
        };
    }

    private void renderItemText(final GuiGraphics graphics,
                                final TaskStatus.Item item,
                                final ResourceRendering rendering,
                                final int x,
                                final int y) {
        int yy = y;
        if (item.stored() > 0) {
            renderItemText(graphics, "stored", rendering, x, yy, item.missing());
            yy += 7;
        }
        if (item.missing() > 0) {
            renderItemText(graphics, "missing", rendering, x, yy, item.missing());
            yy += 7;
        }
        if (item.processing() > 0) {
            renderItemText(graphics, "processing", rendering, x, yy, item.processing());
            yy += 7;
        }
        if (item.scheduled() > 0) {
            renderItemText(graphics, "scheduled", rendering, x, yy, item.scheduled());
            yy += 7;
        }
        if (item.crafting() > 0) {
            renderItemText(graphics, "crafting", rendering, x, yy, item.crafting());
        }
    }

    private void renderItemText(final GuiGraphics graphics,
                                final String type,
                                final ResourceRendering rendering,
                                final int x,
                                final int y,
                                final long amount) {
        SmallText.render(
            graphics,
            font,
            createTranslation("gui", "autocrafting_monitor." + type, rendering.formatAmount(amount, true))
                .getVisualOrderText(),
            x,
            y,
            0x404040,
            false
        );
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (taskItemsScrollbar != null
            && taskItemsScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        if (taskButtonsScrollbar != null && taskButtonsScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (taskItemsScrollbar != null) {
            taskItemsScrollbar.mouseMoved(mx, my);
        }
        if (taskButtonsScrollbar != null) {
            taskButtonsScrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final double mx, final double my, final int button) {
        if (taskItemsScrollbar != null && taskItemsScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        if (taskButtonsScrollbar != null && taskButtonsScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        final boolean didTaskItemsScrollbar = taskItemsScrollbar != null
            && isHoveringOverItems(x, y)
            && taskItemsScrollbar.mouseScrolled(x, y, z, delta);
        final boolean didTaskButtonsScrollbar = !didTaskItemsScrollbar
            && taskButtonsScrollbar != null
            && isHoveringOverTaskButtons(x, y)
            && taskButtonsScrollbar.mouseScrolled(x, y, z, delta);
        return didTaskItemsScrollbar || didTaskButtonsScrollbar || super.mouseScrolled(x, y, z, delta);
    }

    private boolean isHoveringOverItems(final double x, final double y) {
        return isHovering(8, 20, 221, ITEMS_AREA_HEIGHT, x, y);
    }

    private boolean isHoveringOverTaskButtons(final double x, final double y) {
        final int tasksInnerX = getTaskButtonsInnerX() - 1;
        final int tasksInnerY = getTaskButtonsInnerY() - 1;
        return isHovering(tasksInnerX - leftPos, tasksInnerY - topPos, 80, 170, x, y);
    }

    private int getTaskButtonsInnerY() {
        return topPos + 8;
    }

    private int getTaskButtonsInnerX() {
        return leftPos - 83 + 4;
    }

    private void updateTaskButtonsScrollbar() {
        if (taskButtonsScrollbar == null) {
            return;
        }
        final int totalTaskButtons = getMenu().getTasks().size() - TASKS_VISIBLE;
        final int maxOffset = taskButtonsScrollbar.isSmoothScrolling()
            ? totalTaskButtons * TASK_BUTTON_HEIGHT
            : totalTaskButtons;
        taskButtonsScrollbar.setEnabled(maxOffset > 0);
        taskButtonsScrollbar.setMaxOffset(maxOffset);
    }

    @Override
    public void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public void currentTaskChanged(@Nullable final TaskStatus taskStatus) {
        if (cancelButton != null) {
            cancelButton.active = taskStatus != null;
        }
        if (cancelAllButton != null) {
            cancelAllButton.active = !menu.getTasks().isEmpty();
        }
        for (final AutocraftingTaskButton taskButton : taskButtons) {
            taskButton.active = taskStatus == null || !taskButton.getTaskId().equals(taskStatus.info().id());
        }
        if (taskItemsScrollbar == null) {
            return;
        }
        if (taskStatus == null) {
            taskItemsScrollbar.setEnabled(false);
            taskItemsScrollbar.setMaxOffset(0);
            return;
        }
        final int items = taskStatus.items().size();
        final int rows = Math.ceilDiv(items, COLUMNS) - ROWS_VISIBLE;
        taskItemsScrollbar.setMaxOffset(taskItemsScrollbar.isSmoothScrolling() ? rows * ROW_HEIGHT : rows);
        taskItemsScrollbar.setEnabled(rows > 0);
    }

    @Override
    public void taskAdded(final TaskStatus taskStatus) {
        updateTaskButtonsScrollbar();
        final int buttonY = getTaskButtonY(getMenu().getTasks().size() - 1);
        final AutocraftingTaskButton button = new AutocraftingTaskButton(
            getTaskButtonsInnerX(),
            buttonY,
            taskStatus.info(),
            menu::setCurrentTaskId,
            menu::getPercentageCompleted
        );
        button.visible = isTaskButtonVisible(buttonY);
        taskButtons.add(addWidget(button));
    }

    @Override
    public void taskRemoved(final TaskId taskId) {
        updateTaskButtonsScrollbar();
        taskButtons.stream().filter(b -> b.getTaskId().equals(taskId)).findFirst().ifPresent(button -> {
            removeWidget(button);
            taskButtons.remove(button);
        });
        for (int i = 0; i < taskButtons.size(); i++) {
            final AutocraftingTaskButton button = taskButtons.get(i);
            button.setY(getTaskButtonY(i));
            button.visible = isTaskButtonVisible(button.getY());
        }
    }
}
