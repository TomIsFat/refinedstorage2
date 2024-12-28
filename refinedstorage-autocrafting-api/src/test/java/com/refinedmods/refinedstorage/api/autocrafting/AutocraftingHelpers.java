package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;

public final class AutocraftingHelpers {
    private AutocraftingHelpers() {
    }

    public static RootStorage storage(final ResourceAmount... resourceAmounts) {
        final RootStorage storage = new RootStorageImpl();
        storage.addSource(new StorageImpl());
        for (final ResourceAmount resourceAmount : resourceAmounts) {
            storage.insert(resourceAmount.resource(), resourceAmount.amount(), Action.EXECUTE, Actor.EMPTY);
        }
        return storage;
    }

    public static PatternRepository patterns(final Pattern... patterns) {
        final PatternRepository patternRepository = new PatternRepositoryImpl();
        for (final Pattern pattern : patterns) {
            patternRepository.add(pattern);
        }
        return patternRepository;
    }
}
