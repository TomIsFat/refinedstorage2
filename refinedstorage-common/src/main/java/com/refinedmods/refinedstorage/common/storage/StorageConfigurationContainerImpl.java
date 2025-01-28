package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.network.impl.storage.StorageConfiguration;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;

public final class StorageConfigurationContainerImpl implements StorageConfigurationContainer {
    private static final String TAG_INSERT_PRIORITY = "pri";
    private static final String TAG_EXTRACT_PRIORITY = "epri";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_ACCESS_MODE = "am";
    private static final String TAG_VOID_EXCESS = "ve";

    private final StorageConfiguration config;
    private final FilterWithFuzzyMode filter;
    private final Runnable listener;
    private final Supplier<RedstoneMode> redstoneModeSupplier;
    private final Consumer<RedstoneMode> redstoneModeConsumer;

    public StorageConfigurationContainerImpl(final StorageConfiguration config,
                                             final FilterWithFuzzyMode filter,
                                             final Runnable listener,
                                             final Supplier<RedstoneMode> redstoneModeSupplier,
                                             final Consumer<RedstoneMode> redstoneModeConsumer) {
        this.config = config;
        this.filter = filter;
        this.listener = listener;
        this.redstoneModeSupplier = redstoneModeSupplier;
        this.redstoneModeConsumer = redstoneModeConsumer;
    }

    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_INSERT_PRIORITY)) {
            config.setInsertPriority(tag.getInt(TAG_INSERT_PRIORITY));
        }
        if (tag.contains(TAG_EXTRACT_PRIORITY)) {
            config.setExtractPriority(tag.getInt(TAG_EXTRACT_PRIORITY));
        } else {
            config.setExtractPriority(config.getInsertPriority()); // bit of compat
        }
        if (tag.contains(TAG_FILTER_MODE)) {
            config.setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }
        if (tag.contains(TAG_ACCESS_MODE)) {
            config.setAccessMode(AccessModeSettings.getAccessMode(tag.getInt(TAG_ACCESS_MODE)));
        }
        if (tag.contains(TAG_VOID_EXCESS)) {
            config.setVoidExcess(tag.getBoolean(TAG_VOID_EXCESS));
        }
    }

    public void save(final CompoundTag tag) {
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(config.getFilterMode()));
        tag.putInt(TAG_INSERT_PRIORITY, config.getInsertPriority());
        tag.putInt(TAG_EXTRACT_PRIORITY, config.getExtractPriority());
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(config.getAccessMode()));
        tag.putBoolean(TAG_VOID_EXCESS, config.isVoidExcess());
    }

    @Override
    public int getInsertPriority() {
        return config.getInsertPriority();
    }

    @Override
    public void setInsertPriority(final int insertPriority) {
        config.setInsertPriority(insertPriority);
        listener.run();
    }

    @Override
    public int getExtractPriority() {
        return config.getExtractPriority();
    }

    @Override
    public void setExtractPriority(final int extractPriority) {
        config.setExtractPriority(extractPriority);
        listener.run();
    }

    @Override
    public FilterMode getFilterMode() {
        return config.getFilterMode();
    }

    @Override
    public void setFilterMode(final FilterMode filterMode) {
        config.setFilterMode(filterMode);
        listener.run();
    }

    @Override
    public boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    @Override
    public void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
        listener.run();
    }

    @Override
    public AccessMode getAccessMode() {
        return config.getAccessMode();
    }

    @Override
    public void setAccessMode(final AccessMode accessMode) {
        config.setAccessMode(accessMode);
        listener.run();
    }

    @Override
    public boolean isVoidExcess() {
        return config.isVoidExcess();
    }

    @Override
    public void setVoidExcess(final boolean voidExcess) {
        config.setVoidExcess(voidExcess);
        listener.run();
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneModeSupplier.get();
    }

    @Override
    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        redstoneModeConsumer.accept(redstoneMode);
    }
}
