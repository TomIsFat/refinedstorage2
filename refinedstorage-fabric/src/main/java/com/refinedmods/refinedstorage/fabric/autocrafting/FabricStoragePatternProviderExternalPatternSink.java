package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.fabric.api.FabricStorageExternalPatternSinkStrategy;

import java.util.Collection;
import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

class FabricStoragePatternProviderExternalPatternSink implements PlatformPatternProviderExternalPatternSink {
    private final Set<FabricStorageExternalPatternSinkStrategy> strategies;

    FabricStoragePatternProviderExternalPatternSink(
        final Set<FabricStorageExternalPatternSinkStrategy> strategies
    ) {
        this.strategies = strategies;
    }

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        ExternalPatternSink.Result result = ExternalPatternSink.Result.SKIPPED;
        try (Transaction tx = Transaction.openOuter()) {
            for (final FabricStorageExternalPatternSinkStrategy strategy : strategies) {
                final ExternalPatternSink.Result strategyResult = strategy.accept(tx, resources);
                if (strategyResult == ExternalPatternSink.Result.REJECTED) {
                    return strategyResult;
                }
                result = and(result, strategyResult);
            }
            if (action == Action.EXECUTE) {
                tx.commit();
            }
        }
        return result;
    }

    private ExternalPatternSink.Result and(final ExternalPatternSink.Result a,
                                           final ExternalPatternSink.Result b) {
        if (a == ExternalPatternSink.Result.SKIPPED) {
            return b;
        } else if (a == ExternalPatternSink.Result.REJECTED || b == ExternalPatternSink.Result.REJECTED) {
            return ExternalPatternSink.Result.REJECTED;
        } else {
            return ExternalPatternSink.Result.ACCEPTED;
        }
    }

    @Override
    public boolean isEmpty() {
        for (final FabricStorageExternalPatternSinkStrategy strategy : strategies) {
            if (!strategy.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
