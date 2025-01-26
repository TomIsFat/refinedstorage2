package com.refinedmods.refinedstorage.api.autocrafting.task;

import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface ExternalPatternInputSinkKey {
    String getName();
}
