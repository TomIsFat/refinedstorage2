package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

public enum AutocrafterManagerViewType {
    VISIBLE,
    NOT_FULL,
    ALL;

    AutocrafterManagerViewType toggle() {
        return switch (this) {
            case VISIBLE -> NOT_FULL;
            case NOT_FULL -> ALL;
            case ALL -> VISIBLE;
        };
    }
}
