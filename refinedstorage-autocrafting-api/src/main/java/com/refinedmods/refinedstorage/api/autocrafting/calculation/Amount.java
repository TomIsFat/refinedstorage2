package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

record Amount(long iterations, long amountPerIteration) {
    public long getTotal() {
        return iterations * amountPerIteration;
    }

    static Amount of(final Pattern pattern, final ResourceKey resource, final long requestedAmount) {
        final long amountPerIteration = pattern.outputs()
            .stream()
            .filter(output -> output.resource().equals(resource))
            .mapToLong(ResourceAmount::amount)
            .sum();
        final long iterations = ((requestedAmount - 1) / amountPerIteration) + 1;
        return new Amount(iterations, amountPerIteration);
    }
}
