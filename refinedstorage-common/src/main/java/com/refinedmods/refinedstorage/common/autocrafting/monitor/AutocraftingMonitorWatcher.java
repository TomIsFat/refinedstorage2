package com.refinedmods.refinedstorage.common.autocrafting.monitor;

@FunctionalInterface
public interface AutocraftingMonitorWatcher {
    void activeChanged(boolean active);
}
