package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.autocrafting.ProcessingPatternState;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToLongFunction;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import static java.util.Objects.requireNonNullElse;

class ProcessingMatrixInputResourceContainer extends ResourceContainerImpl {
    private static final String TAG_ALLOWED_TAG_IDS = "allowedTagIds";
    private static final String TAG_INDEX = "index";
    private static final String TAG_IDS = "ids";

    private final List<@NullableType Set<ResourceLocation>> allowedTagIds;

    ProcessingMatrixInputResourceContainer(final int size,
                                           final ToLongFunction<ResourceKey> maxAmountProvider,
                                           final ResourceFactory primaryResourceFactory,
                                           final Set<ResourceFactory> alternativeResourceFactories) {
        super(size, maxAmountProvider, primaryResourceFactory, alternativeResourceFactories);
        this.allowedTagIds = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            allowedTagIds.add(null);
        }
    }

    void set(final int index, final ProcessingPatternState.ProcessingIngredient processingIngredient) {
        setSilently(index, processingIngredient.input());
        allowedTagIds.set(index, new HashSet<>(processingIngredient.allowedAlternativeIds()));
        changed();
    }

    Optional<ProcessingPatternState.ProcessingIngredient> getIngredient(final int index) {
        return Optional.ofNullable(get(index)).map(input -> getIngredient(index, input));
    }

    private ProcessingPatternState.ProcessingIngredient getIngredient(final int index, final ResourceAmount input) {
        final List<ResourceLocation> ids = allowedTagIds.get(index) == null
            ? Collections.emptyList()
            : new ArrayList<>(allowedTagIds.get(index));
        return new ProcessingPatternState.ProcessingIngredient(input, ids);
    }

    Set<ResourceLocation> getAllowedTagIds(final int index) {
        return Collections.unmodifiableSet(requireNonNullElse(allowedTagIds.get(index), Collections.emptySet()));
    }

    void setAllowedTagIds(final int index, final Set<ResourceLocation> ids) {
        if (index < 0 || index >= allowedTagIds.size()) {
            return;
        }
        allowedTagIds.set(index, ids);
        changed();
    }

    @Override
    protected void setSilently(final int index, final ResourceAmount resourceAmount) {
        super.setSilently(index, resourceAmount);
        allowedTagIds.set(index, new HashSet<>());
    }

    @Override
    protected void removeSilently(final int index) {
        super.removeSilently(index);
        allowedTagIds.set(index, null);
    }

    @Override
    public void fromTag(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.fromTag(tag, provider);
        if (!tag.contains(TAG_ALLOWED_TAG_IDS)) {
            return;
        }
        final ListTag allowedTagIdsTag = tag.getList(TAG_ALLOWED_TAG_IDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < allowedTagIdsTag.size(); ++i) {
            final CompoundTag allowedTagEntry = allowedTagIdsTag.getCompound(i);
            final int index = allowedTagEntry.getInt(TAG_INDEX);
            final ListTag idsTag = allowedTagEntry.getList(TAG_IDS, Tag.TAG_STRING);
            final Set<ResourceLocation> ids = new HashSet<>();
            for (int j = 0; j < idsTag.size(); ++j) {
                ids.add(ResourceLocation.parse(idsTag.getString(j)));
            }
            allowedTagIds.set(index, ids);
        }
    }

    @Override
    public CompoundTag toTag(final HolderLookup.Provider provider) {
        final CompoundTag tag = super.toTag(provider);
        final ListTag allowedTagIdsTag = new ListTag();
        for (int i = 0; i < allowedTagIds.size(); ++i) {
            final Set<ResourceLocation> ids = allowedTagIds.get(i);
            if (ids == null) {
                continue;
            }
            final CompoundTag allowedTagEntry = new CompoundTag();
            allowedTagEntry.putInt(TAG_INDEX, i);
            final ListTag idsTag = new ListTag();
            for (final ResourceLocation id : ids) {
                idsTag.add(StringTag.valueOf(id.toString()));
            }
            allowedTagEntry.put(TAG_IDS, idsTag);
            allowedTagIdsTag.add(allowedTagEntry);
        }
        tag.put(TAG_ALLOWED_TAG_IDS, allowedTagIdsTag);
        return tag;
    }
}
