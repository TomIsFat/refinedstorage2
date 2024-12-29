package com.refinedmods.refinedstorage.api.network.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;
import com.refinedmods.refinedstorage.api.network.NetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.8")
public interface AutocraftingNetworkComponent extends NetworkComponent, PreviewProvider, TaskStatusProvider {
    void addListener(PatternListener listener);

    void removeListener(PatternListener listener);

    Set<Pattern> getPatterns();

    List<Pattern> getPatternsByOutput(ResourceKey output);

    Set<ResourceKey> getOutputs();

    boolean contains(AutocraftingNetworkComponent component);
}
