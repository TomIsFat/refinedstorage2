package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.Amount;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Optional;

public class TaskCraftingCalculatorListener implements CraftingCalculatorListener<MutableTaskPlan> {
    private MutableTaskPlan task;

    private TaskCraftingCalculatorListener(final MutableTaskPlan task) {
        this.task = task;
    }

    public static Optional<TaskPlan> calculatePlan(final CraftingCalculator calculator,
                                                   final ResourceKey resource,
                                                   final long amount) {
        final TaskCraftingCalculatorListener listener = new TaskCraftingCalculatorListener(new MutableTaskPlan());
        calculator.calculate(resource, amount, listener);
        return listener.task.getPlan();
    }

    @Override
    public CraftingCalculatorListener<MutableTaskPlan> childCalculationStarted(final Pattern pattern,
                                                                               final ResourceKey resource,
                                                                               final Amount amount) {
        final MutableTaskPlan copy = task.copy();
        copy.addOrUpdatePattern(pattern, amount.iterations());
        return new TaskCraftingCalculatorListener(copy);
    }

    @Override
    public void childCalculationCompleted(final CraftingCalculatorListener<MutableTaskPlan> childListener) {
        this.task = childListener.getData();
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        task.setMissing();
    }

    @Override
    public void ingredientUsed(final Pattern pattern,
                               final int ingredientIndex,
                               final ResourceKey resource,
                               final long amount) {
        task.addUsedIngredient(pattern, ingredientIndex, resource, amount);
    }

    @Override
    public void ingredientExtractedFromStorage(final ResourceKey resource, final long amount) {
        task.addToExtract(resource, amount);
    }

    @Override
    public MutableTaskPlan getData() {
        return task;
    }
}
