package com.refinedmods.refinedstorage.api.autocrafting;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.COBBLESTONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.CRAFTING_TABLE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_INGOT;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_ORE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.IRON_PICKAXE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SIGN;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SMOOTH_STONE;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STICKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.STONE;

public final class PatternFixtures {
    public static final Pattern OAK_PLANKS_PATTERN = pattern()
        .ingredient(OAK_LOG, 1)
        .output(OAK_PLANKS, 4)
        .build();
    public static final Pattern SPRUCE_PLANKS_PATTERN = pattern()
        .ingredient(SPRUCE_LOG, 1)
        .output(SPRUCE_PLANKS, 4)
        .build();
    public static final Pattern CRAFTING_TABLE_PATTERN = pattern()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .output(CRAFTING_TABLE, 1)
        .build();
    public static final Pattern CRAFTING_TABLE_YIELD_2X_PATTERN = pattern()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .ingredient(1).input(OAK_PLANKS).input(SPRUCE_PLANKS).end()
        .output(CRAFTING_TABLE, 2)
        .build();
    public static final Pattern IRON_INGOT_PATTERN = pattern(PatternType.EXTERNAL)
        .ingredient(IRON_ORE, 1)
        .output(IRON_INGOT, 1)
        .build();
    public static final Pattern IRON_PICKAXE_PATTERN = pattern()
        .ingredient(IRON_INGOT, 1)
        .ingredient(IRON_INGOT, 1)
        .ingredient(IRON_INGOT, 1)
        .ingredient(STICKS, 2)
        .output(IRON_PICKAXE, 1)
        .build();
    public static final Pattern STONE_PATTERN = pattern(PatternType.EXTERNAL)
        .ingredient(COBBLESTONE, 1)
        .output(STONE, 1)
        .build();
    public static final Pattern SMOOTH_STONE_PATTERN = pattern(PatternType.EXTERNAL)
        .ingredient(STONE, 1)
        .output(SMOOTH_STONE, 1)
        .build();
    public static final Pattern STICKS_PATTERN = pattern()
        .ingredient(OAK_PLANKS, 2)
        .output(STICKS, 4)
        .build();
    public static final Pattern SIGN_PATTERN = pattern()
        .ingredient(OAK_PLANKS, 6)
        .ingredient(STICKS, 1)
        .output(SIGN, 3)
        .build();

    private PatternFixtures() {
    }
}
