package com.refinedmods.refinedstorage.api.autocrafting.calculation;

public class NumberOverflowDuringCalculationException extends RuntimeException {
    NumberOverflowDuringCalculationException() {
        super("Invalid amount");
    }
}
