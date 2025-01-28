package com.refinedmods.refinedstorage.common.grid;

public enum AutocraftableResourceHint {
    AUTOCRAFTABLE(0x80FFA500),
    PATTERN_IN_INVENTORY(0x3FFFA500);

    private final int color;

    AutocraftableResourceHint(final int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
