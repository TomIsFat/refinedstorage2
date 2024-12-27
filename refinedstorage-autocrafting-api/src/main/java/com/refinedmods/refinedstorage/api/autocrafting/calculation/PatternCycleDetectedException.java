package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;

public class PatternCycleDetectedException extends CalculationException {
    private final Pattern pattern;

    PatternCycleDetectedException(final Pattern pattern) {
        super("Pattern loop detected in pattern " + pattern);
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return this.pattern;
    }
}
