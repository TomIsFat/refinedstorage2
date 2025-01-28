package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;
import java.util.Optional;

public record StorageBlockData(long stored, long capacity, List<Optional<ResourceAmount>> resources) {
}
