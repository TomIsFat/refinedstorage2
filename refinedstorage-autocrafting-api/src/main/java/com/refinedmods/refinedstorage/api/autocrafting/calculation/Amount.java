package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

public record Amount(long iterations, long amountPerIteration) {
    public Amount {
        CoreValidations.validateLargerThanZero(iterations, "Iterations");
        CoreValidations.validateLargerThanZero(amountPerIteration, "Amount per iteration");
    }

    public long getTotal() {
        return iterations * amountPerIteration;
    }

    static Amount of(final Pattern pattern, final ResourceKey resource, final long requestedAmount) {
        final long amountPerIteration = pattern.layout().outputs()
            .stream()
            .filter(output -> output.resource().equals(resource))
            .mapToLong(ResourceAmount::amount)
            .sum();
        final long iterations = ((requestedAmount - 1) / amountPerIteration) + 1;
        return new Amount(iterations, amountPerIteration);
    }
}
