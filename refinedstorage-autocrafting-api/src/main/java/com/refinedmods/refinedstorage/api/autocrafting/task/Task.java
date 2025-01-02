package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface Task {
    TaskState getState();

    void step(RootStorage rootStorage, ExternalPatternInputSink externalPatternInputSink);
}
