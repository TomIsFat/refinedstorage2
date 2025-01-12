package com.refinedmods.refinedstorage.api.network.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.8")
public interface PatternProvider extends PatternProviderExternalPatternInputSink, StepBehavior {
    void onAddedIntoContainer(ParentContainer parentContainer);

    void onRemovedFromContainer(ParentContainer parentContainer);

    default boolean contains(AutocraftingNetworkComponent component) {
        return false;
    }

    void addTask(Task task);
}
