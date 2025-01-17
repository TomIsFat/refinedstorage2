package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.10")
public record TaskStatus(TaskInfo info, float percentageCompleted, List<Item> items) {
    public record TaskInfo(TaskId id, ResourceKey resource, long amount, long startTime) {
    }

    public record Item(
        ItemType type,
        ResourceKey resource,
        // what is stored internally?
        long stored,
        long missing,
        // what is currently processing?
        long processing, // (originalIterationsToSendToSink - iterationsToSendToSink)*input
        // what are we still going to process?
        long scheduled, // iterationsToSendToSink*input
        // what do we need to still craft?
        long crafting // iterationsRemaining*output
    ) {
    }

    public enum ItemType {
        NORMAL,
        MACHINE_DOES_NOT_ACCEPT_RESOURCE,
        NO_MACHINE_FOUND,
        AUTOCRAFTER_IS_LOCKED
    }
}
