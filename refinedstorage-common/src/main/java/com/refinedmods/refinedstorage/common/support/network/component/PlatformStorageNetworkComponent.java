package com.refinedmods.refinedstorage.common.support.network.component;

import com.refinedmods.refinedstorage.api.network.impl.storage.StorageNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.api.support.resource.list.FuzzyResourceList;
import com.refinedmods.refinedstorage.common.support.resource.list.FuzzyResourceListImpl;

import java.util.Collection;

public class PlatformStorageNetworkComponent extends StorageNetworkComponentImpl implements FuzzyRootStorage {
    private final FuzzyResourceList fuzzyResourceList;

    public PlatformStorageNetworkComponent() {
        this(new FuzzyResourceListImpl(MutableResourceListImpl.create()));
    }

    private PlatformStorageNetworkComponent(final FuzzyResourceListImpl fuzzyResourceList) {
        super(fuzzyResourceList);
        this.fuzzyResourceList = fuzzyResourceList;
    }

    @Override
    public Collection<ResourceKey> getFuzzy(final ResourceKey resource) {
        return fuzzyResourceList.getFuzzy(resource);
    }
}
