package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternInputSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternInputSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.fabric.api.FabricStorageExternalPatternInputSinkStrategy;

import java.util.Collection;
import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

class FabricStoragePatternProviderExternalPatternInputSink implements PatternProviderExternalPatternInputSink {
    private final Set<FabricStorageExternalPatternInputSinkStrategy> strategies;

    FabricStoragePatternProviderExternalPatternInputSink(
        final Set<FabricStorageExternalPatternInputSinkStrategy> strategies
    ) {
        this.strategies = strategies;
    }

    @Override
    public ExternalPatternInputSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        ExternalPatternInputSink.Result result = ExternalPatternInputSink.Result.SKIPPED;
        try (Transaction tx = Transaction.openOuter()) {
            for (final FabricStorageExternalPatternInputSinkStrategy strategy : strategies) {
                final ExternalPatternInputSink.Result strategyResult = strategy.accept(tx, resources);
                if (strategyResult == ExternalPatternInputSink.Result.REJECTED) {
                    return strategyResult;
                }
                result = result.and(strategyResult);
            }
            if (action == Action.EXECUTE) {
                tx.commit();
            }
        }
        return result;
    }
}
