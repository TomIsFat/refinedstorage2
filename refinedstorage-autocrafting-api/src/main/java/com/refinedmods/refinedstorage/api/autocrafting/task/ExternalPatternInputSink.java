package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
@FunctionalInterface
public interface ExternalPatternInputSink {
    Result accept(Pattern pattern, Collection<ResourceAmount> resources, Action action);

    enum Result {
        ACCEPTED,
        REJECTED,
        SKIPPED,
        LOCKED;

        public Result and(final Result other) {
            if (this == SKIPPED) {
                return other;
            } else if (this == REJECTED || other == REJECTED) {
                return REJECTED;
            } else {
                return ACCEPTED;
            }
        }
    }
}
