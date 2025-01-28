package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorScreen.TASK_BUTTON_HEIGHT;
import static com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorScreen.TASK_BUTTON_WIDTH;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class AutocraftingTaskButton extends AbstractButton {
    private final TaskStatus.TaskInfo task;
    private final TextMarquee text;
    private final Consumer<TaskId> onPress;
    private final PercentageCompletedProvider percentageCompletedProvider;

    AutocraftingTaskButton(final int x,
                           final int y,
                           final TaskStatus.TaskInfo task,
                           final Consumer<TaskId> onPress,
                           final PercentageCompletedProvider percentageCompletedProvider) {
        super(x, y, TASK_BUTTON_WIDTH, TASK_BUTTON_HEIGHT, Component.empty());
        this.task = task;
        final ResourceKey resource = task.resource();
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        this.text = new TextMarquee(
            rendering.getDisplayName(resource),
            TASK_BUTTON_WIDTH - 16 - 4 - 4 - 4,
            0xFFFFFF,
            true,
            true
        );
        this.onPress = onPress;
        this.percentageCompletedProvider = percentageCompletedProvider;
    }

    TaskId getTaskId() {
        return task.id();
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics,
                                final int mouseX,
                                final int mouseY,
                                final float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        renderResourceIcon(graphics);
        final int yOffset = SmallText.isSmall() ? 5 : 3;
        final int textX = getX() + 3 + 16 + 3;
        final int textY = getY() + yOffset;
        text.render(graphics, textX, textY, Minecraft.getInstance().font, isHovered);
        final int ySpacing = SmallText.isSmall() ? 7 : 8;
        final long percentageCompleted = Math.round(
            percentageCompletedProvider.getPercentageCompleted(task.id()) * 100
        );
        SmallText.render(graphics, Minecraft.getInstance().font, percentageCompleted + "%", textX, textY + ySpacing,
            0xFFFFFF, true, SmallText.DEFAULT_SCALE);
        updateTooltip();
    }

    private void renderResourceIcon(final GuiGraphics graphics) {
        final ResourceKey resource = task.resource();
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        final int resourceX = getX() + 3;
        final int resourceY = getY() + 4;
        rendering.render(resource, graphics, resourceX, resourceY);
        ResourceSlotRendering.renderAmount(graphics, resourceX, resourceY, task.amount(), rendering);
    }

    private void updateTooltip() {
        if (isHovered) {
            final String runningTime = getRunningTimeText();
            setTooltip(Tooltip.create(
                createTranslation("gui", "autocrafting_monitor.running_time", runningTime)
            ));
        } else {
            setTooltip(null);
        }
    }

    private String getRunningTimeText() {
        final int totalSecs = (int) (System.currentTimeMillis() - task.startTime()) / 1000;
        final int hours = totalSecs / 3600;
        final int minutes = (totalSecs % 3600) / 60;
        final int seconds = totalSecs % 60;
        final String runningTime;
        if (hours > 0) {
            runningTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            runningTime = String.format("%02d:%02d", minutes, seconds);
        }
        return runningTime;
    }

    @Override
    public void onPress() {
        onPress.accept(task.id());
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // no op
    }

    interface PercentageCompletedProvider {
        double getPercentageCompleted(TaskId taskId);
    }
}
