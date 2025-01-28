package com.refinedmods.refinedstorage.api.storage.composite;

import com.refinedmods.refinedstorage.api.storage.Storage;

import java.util.Comparator;

class PrioritizedStorageComparator implements Comparator<Storage> {
    static final Comparator<Storage> INSERT = new PrioritizedStorageComparator(PriorityProvider::getInsertPriority);
    static final Comparator<Storage> EXTRACT = new PrioritizedStorageComparator(PriorityProvider::getExtractPriority);

    private final PriorityExtractor priorityExtractor;

    private PrioritizedStorageComparator(final PriorityExtractor priorityExtractor) {
        this.priorityExtractor = priorityExtractor;
    }

    private int extractPriority(final Storage storage) {
        if (storage instanceof PriorityProvider priorityProvider) {
            return priorityExtractor.getPriority(priorityProvider);
        }
        return 0;
    }

    @Override
    public int compare(final Storage a, final Storage b) {
        return Integer.compare(extractPriority(b), extractPriority(a));
    }

    @FunctionalInterface
    private interface PriorityExtractor {
        int getPriority(PriorityProvider provider);
    }
}
