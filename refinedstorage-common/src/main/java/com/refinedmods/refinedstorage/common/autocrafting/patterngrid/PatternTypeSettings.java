package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

class PatternTypeSettings {
    private static final int CRAFTING = 0;
    private static final int PROCESSING = 1;
    private static final int STONECUTTER = 2;
    private static final int SMITHING_TABLE = 3;

    private PatternTypeSettings() {
    }

    static PatternType getPatternType(final int patternType) {
        return switch (patternType) {
            case PROCESSING -> PatternType.PROCESSING;
            case STONECUTTER -> PatternType.STONECUTTER;
            case SMITHING_TABLE -> PatternType.SMITHING_TABLE;
            default -> PatternType.CRAFTING;
        };
    }

    static int getPatternType(final PatternType patternType) {
        return switch (patternType) {
            case CRAFTING -> CRAFTING;
            case PROCESSING -> PROCESSING;
            case STONECUTTER -> STONECUTTER;
            case SMITHING_TABLE -> SMITHING_TABLE;
        };
    }
}
