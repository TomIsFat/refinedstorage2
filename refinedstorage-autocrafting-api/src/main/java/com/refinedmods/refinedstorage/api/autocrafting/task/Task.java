package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageListener;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface Task extends RootStorageListener {
    TaskId getId();

    TaskState getState();
    
    Collection<ResourceAmount> copyInternalStorageState();

    void step(RootStorage rootStorage, ExternalPatternInputSink externalPatternInputSink, StepBehavior stepBehavior);

    void cancel();
}
