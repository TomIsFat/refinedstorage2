package com.refinedmods.refinedstorage.common.grid;

public enum GridViewType {
    ALL,
    AUTOCRAFTABLE,
    NON_AUTOCRAFTABLE;

    boolean accepts(final boolean autocraftable) {
        return switch (this) {
            case ALL -> true;
            case AUTOCRAFTABLE -> autocraftable;
            case NON_AUTOCRAFTABLE -> !autocraftable;
        };
    }
}
