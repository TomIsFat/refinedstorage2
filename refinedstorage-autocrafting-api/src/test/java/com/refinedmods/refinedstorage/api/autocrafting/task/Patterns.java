package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;

final class Patterns {
    static final Pattern OAK_PLANKS_PATTERN = pattern()
        .ingredient(OAK_LOG, 1)
        .output(OAK_PLANKS, 4)
        .build();
    static final Pattern SPRUCE_PLANKS_PATTERN = pattern()
        .ingredient(SPRUCE_LOG, 1)
        .output(SPRUCE_PLANKS, 4)
        .build();
    static final Pattern CRAFTING_TABLE_PATTERN = pattern()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .output(CRAFTING_TABLE, 1)
        .build();

    private Patterns() {
    }
}
