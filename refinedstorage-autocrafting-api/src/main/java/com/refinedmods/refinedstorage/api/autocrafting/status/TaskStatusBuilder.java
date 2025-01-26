package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class TaskStatusBuilder {
    private final TaskStatus.TaskInfo info;
    private final Map<ResourceKey, MutableItem> items = new LinkedHashMap<>();

    public TaskStatusBuilder(final TaskId id, final ResourceKey resource, final long amount, final long startTime) {
        this.info = new TaskStatus.TaskInfo(id, resource, amount, startTime);
    }

    public TaskStatusBuilder stored(final ResourceKey resource, final long stored) {
        CoreValidations.validateLargerThanZero(stored, "Stored");
        get(resource).stored += stored;
        return this;
    }

    public TaskStatusBuilder processing(final ResourceKey resource, final long processing) {
        CoreValidations.validateLargerThanZero(processing, "Processing");
        get(resource).processing += processing;
        return this;
    }

    public TaskStatusBuilder scheduled(final ResourceKey resource, final long scheduled) {
        CoreValidations.validateLargerThanZero(scheduled, "Crafting");
        get(resource).scheduled += scheduled;
        return this;
    }

    public TaskStatusBuilder crafting(final ResourceKey resource, final long crafting) {
        CoreValidations.validateLargerThanZero(crafting, "Crafting");
        get(resource).crafting += crafting;
        return this;
    }

    public TaskStatusBuilder rejected(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.REJECTED;
        return this;
    }

    public TaskStatusBuilder noneFound(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.NONE_FOUND;
        return this;
    }

    public TaskStatusBuilder locked(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.LOCKED;
        return this;
    }

    private MutableItem get(final ResourceKey resource) {
        return items.computeIfAbsent(resource, key -> new MutableItem(TaskStatus.ItemType.NORMAL));
    }

    public TaskStatus build(final double percentageCompleted) {
        return new TaskStatus(info, percentageCompleted, items.entrySet().stream().map(entry -> new TaskStatus.Item(
            entry.getKey(),
            entry.getValue().type,
            entry.getValue().stored,
            entry.getValue().processing,
            entry.getValue().scheduled,
            entry.getValue().crafting
        )).toList());
    }

    private static class MutableItem {
        private TaskStatus.ItemType type;
        private long stored;
        private long processing;
        private long scheduled;
        private long crafting;

        private MutableItem(final TaskStatus.ItemType type) {
            this.type = type;
        }
    }
}
