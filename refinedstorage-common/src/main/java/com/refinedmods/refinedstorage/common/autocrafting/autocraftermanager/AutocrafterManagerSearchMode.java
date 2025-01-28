package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import net.minecraft.world.level.Level;

public enum AutocrafterManagerSearchMode {
    ALL,
    PATTERN_INPUTS,
    PATTERN_OUTPUTS,
    AUTOCRAFTER_NAMES;

    AutocrafterManagerSearchMode toggle() {
        return switch (this) {
            case ALL -> PATTERN_INPUTS;
            case PATTERN_INPUTS -> PATTERN_OUTPUTS;
            case PATTERN_OUTPUTS -> AUTOCRAFTER_NAMES;
            case AUTOCRAFTER_NAMES -> ALL;
        };
    }

    boolean isSlotVisible(final AutocrafterManagerContainerMenu.ViewGroup group,
                          final Level level,
                          final String normalizedQuery,
                          final int index) {
        return switch (this) {
            case ALL -> group.nameContains(normalizedQuery)
                || group.hasPatternInput(level, normalizedQuery, index)
                || group.hasPatternOutput(level, normalizedQuery, index);
            case PATTERN_INPUTS -> group.hasPatternInput(level, normalizedQuery, index);
            case PATTERN_OUTPUTS -> group.hasPatternOutput(level, normalizedQuery, index);
            case AUTOCRAFTER_NAMES -> group.nameContains(normalizedQuery);
        };
    }
}
