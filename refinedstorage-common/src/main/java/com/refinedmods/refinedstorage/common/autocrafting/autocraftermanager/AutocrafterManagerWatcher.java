package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

@FunctionalInterface
interface AutocrafterManagerWatcher {
    void activeChanged(boolean active);
}
