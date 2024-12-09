package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class PreviewBuilder {
    private final PreviewType type;
    private final Map<ResourceKey, MutablePreviewItem> items = new LinkedHashMap<>();

    private PreviewBuilder(final PreviewType type) {
        this.type = type;
    }

    public static PreviewBuilder ofType(final PreviewType type) {
        return new PreviewBuilder(type);
    }

    private MutablePreviewItem get(final ResourceKey resource) {
        return items.computeIfAbsent(resource, key -> new MutablePreviewItem());
    }

    public PreviewBuilder addAvailable(final ResourceKey resource, final long amount) {
        CoreValidations.validateLargerThanZero(amount, "Available amount must be larger than 0");
        get(resource).available += amount;
        return this;
    }

    public PreviewBuilder addMissing(final ResourceKey resource, final long amount) {
        CoreValidations.validateLargerThanZero(amount, "Missing amount must be larger than 0");
        get(resource).missing += amount;
        return this;
    }

    public PreviewBuilder addToCraft(final ResourceKey resource, final long amount) {
        CoreValidations.validateLargerThanZero(amount, "To craft amount must be larger than 0");
        get(resource).toCraft += amount;
        return this;
    }

    public Preview build() {
        return new Preview(type, items.entrySet()
            .stream()
            .map(entry -> entry.getValue().toPreviewItem(entry.getKey()))
            .toList());
    }

    private static class MutablePreviewItem {
        private long available;
        private long missing;
        private long toCraft;

        private PreviewItem toPreviewItem(final ResourceKey resource) {
            return new PreviewItem(resource, available, missing, toCraft);
        }
    }
}
