package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;

interface AutocraftingMonitor extends TaskStatusProvider {
    void addWatcher(AutocraftingMonitorWatcher watcher);

    void removeWatcher(AutocraftingMonitorWatcher watcher);

    boolean isAutocraftingMonitorActive();
}
