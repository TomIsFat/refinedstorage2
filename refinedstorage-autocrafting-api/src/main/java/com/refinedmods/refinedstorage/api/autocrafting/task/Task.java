package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageListener;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface Task extends RootStorageListener {
    Actor getActor();

    boolean shouldNotify();

    ResourceKey getResource();

    long getAmount();

    TaskId getId();

    TaskState getState();

    boolean step(RootStorage rootStorage, ExternalPatternSinkProvider sinkProvider, StepBehavior stepBehavior);

    void cancel();

    TaskStatus getStatus();
}
