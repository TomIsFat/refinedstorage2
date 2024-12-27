package com.refinedmods.refinedstorage.api.autocrafting.calculation;

public class NumberOverflowDuringCalculationException extends CalculationException {
    NumberOverflowDuringCalculationException() {
        super("Invalid amount");
    }
}
