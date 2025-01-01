package com.refinedmods.refinedstorage.api.network.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.8")
public interface ParentContainer {
    void add(Pattern pattern, int priority);

    void remove(Pattern pattern);

    void update(Pattern pattern, int priority);
}
