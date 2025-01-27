package com.refinedmods.refinedstorage.api.network.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.8")
public interface ParentContainer {
    void add(PatternProvider provider, Pattern pattern, int priority);

    void remove(PatternProvider provider, Pattern pattern);

    void update(Pattern pattern, int priority);

    void taskAdded(PatternProvider provider, Task task);

    void taskRemoved(Task task);

    void taskCompleted(Task task);

    void taskChanged(Task task);
}
