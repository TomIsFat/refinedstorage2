package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.10")
public record TaskStatus(TaskInfo info, double percentageCompleted, List<Item> items) {
    public record TaskInfo(TaskId id, ResourceKey resource, long amount, long startTime) {
    }

    public record Item(
        ResourceKey resource,
        ItemType type,
        @Nullable ExternalPatternSinkKey sinkKey,
        long stored,
        long processing,
        long scheduled,
        long crafting
    ) {
    }

    public enum ItemType {
        NORMAL,
        REJECTED,
        NONE_FOUND,
        LOCKED
    }
}
