package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public enum PatternType implements StringRepresentable {
    CRAFTING(Items.CRAFTING_TABLE.getDefaultInstance(), "crafting"),
    PROCESSING(Items.FURNACE.getDefaultInstance(), "processing"),
    STONECUTTER(Items.STONECUTTER.getDefaultInstance(), "stonecutter"),
    SMITHING_TABLE(Items.SMITHING_TABLE.getDefaultInstance(), "smithing_table");

    public static final Codec<PatternType> CODEC = StringRepresentable.fromValues(PatternType::values);

    private final ItemStack stack;
    private final String name;
    private final Component translatedName;

    PatternType(final ItemStack stack, final String name) {
        this.stack = stack;
        this.name = name;
        this.translatedName = createTranslation("misc", "pattern." + name);
    }

    ItemStack getStack() {
        return stack;
    }

    Component getTranslatedName() {
        return translatedName;
    }

    PatternGridRenderer createRenderer(final PatternGridContainerMenu menu,
                                       final int leftPos,
                                       final int topPos,
                                       final int x,
                                       final int y) {
        return switch (this) {
            case CRAFTING -> new CraftingPatternGridRenderer(menu, leftPos, x, y);
            case PROCESSING -> new ProcessingPatternGridRenderer(menu, leftPos, topPos, x, y);
            case STONECUTTER -> new StonecutterPatternGridRenderer(menu, leftPos, x, y);
            case SMITHING_TABLE -> new SmithingTablePatternGridRenderer(menu, leftPos, topPos, x, y);
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
