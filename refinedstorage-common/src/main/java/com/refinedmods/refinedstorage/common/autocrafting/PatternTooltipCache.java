package com.refinedmods.refinedstorage.common.autocrafting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public final class PatternTooltipCache {
    private static final Map<UUID, ClientTooltipComponent> CACHE = new HashMap<>();

    private PatternTooltipCache() {
    }

    public static ClientTooltipComponent getComponent(final PatternItem.CraftingPatternTooltipComponent key) {
        if (CACHE.size() > 1000) {
            CACHE.clear();
        }
        return CACHE.computeIfAbsent(key.id(), id -> new CraftingPatternClientTooltipComponent(
            key.width(),
            key.height(),
            key.craftingPattern()
        ));
    }

    public static ClientTooltipComponent getComponent(final PatternItem.ProcessingPatternTooltipComponent key) {
        if (CACHE.size() > 1000) {
            CACHE.clear();
        }
        return CACHE.computeIfAbsent(key.id(), id -> new ProcessingPatternClientTooltipComponent(key.state()));
    }
}
