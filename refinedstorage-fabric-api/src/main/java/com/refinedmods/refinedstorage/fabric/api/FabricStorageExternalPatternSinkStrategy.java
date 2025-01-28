package com.refinedmods.refinedstorage.fabric.api;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.Collection;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface FabricStorageExternalPatternSinkStrategy {
    ExternalPatternSink.Result accept(Transaction tx, Collection<ResourceAmount> resources);

    boolean isEmpty();
}
