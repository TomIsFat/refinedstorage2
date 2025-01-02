package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class ExternalPatternInputSinkBuilder {
    private final Map<Pattern, Sink> sinks = new HashMap<>();

    private ExternalPatternInputSinkBuilder() {
    }

    static ExternalPatternInputSinkBuilder externalPatternInputSink() {
        return new ExternalPatternInputSinkBuilder();
    }

    Storage storageSink(final Pattern pattern) {
        final Storage storage = new StorageImpl();
        sinks.put(pattern, new StorageSink(storage));
        return storage;
    }

    ExternalPatternInputSink build() {
        return (pattern, resources, action) -> {
            final Sink sink = sinks.get(pattern);
            return sink != null && sink.accept(resources, action);
        };
    }

    private interface Sink {
        boolean accept(Collection<ResourceAmount> resources, Action action);
    }

    private static class StorageSink implements Sink {
        private final Storage storage;

        private StorageSink(final Storage storage) {
            this.storage = storage;
        }

        @Override
        public boolean accept(final Collection<ResourceAmount> resources, final Action action) {
            if (action == Action.EXECUTE) {
                return accept(resources);
            }
            return acceptsSimulated(resources);
        }

        private boolean accept(final Collection<ResourceAmount> resources) {
            for (final ResourceAmount resourceAmount : resources) {
                final long inserted = storage.insert(
                    resourceAmount.resource(), resourceAmount.amount(), Action.EXECUTE, Actor.EMPTY
                );
                if (inserted != resourceAmount.amount()) {
                    throw new IllegalStateException();
                }
            }
            return true;
        }

        private boolean acceptsSimulated(final Collection<ResourceAmount> resources) {
            final Storage storageCopy = copyStorage();
            for (final ResourceAmount resourceAmount : resources) {
                final long inserted = storageCopy.insert(
                    resourceAmount.resource(),
                    resourceAmount.amount(),
                    Action.EXECUTE,
                    Actor.EMPTY
                );
                if (inserted != resourceAmount.amount()) {
                    return false;
                }
            }
            return true;
        }

        private Storage copyStorage() {
            final Storage storageCopy = new StorageImpl();
            storage.getAll().forEach(r -> storageCopy.insert(r.resource(), r.amount(), Action.EXECUTE, Actor.EMPTY));
            return storageCopy;
        }
    }
}
