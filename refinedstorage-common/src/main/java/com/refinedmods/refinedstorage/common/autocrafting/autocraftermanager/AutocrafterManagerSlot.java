package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import com.refinedmods.refinedstorage.common.autocrafting.PatternSlot;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;

class AutocrafterManagerSlot extends PatternSlot {
    private final int originalY;
    private final int startY;
    private final int endY;
    private final boolean active;

    AutocrafterManagerSlot(final Container container,
                           final Level level,
                           final int slot,
                           final int x,
                           final int y,
                           final IntIntPair startEndY,
                           final boolean active) {
        super(container, slot, x, y, level);
        this.originalY = y;
        this.startY = startEndY.firstInt();
        this.endY = startEndY.secondInt();
        this.active = active;
    }

    int getOriginalY() {
        return originalY;
    }

    @Override
    public boolean isActive() {
        return y >= startY && y < endY && active;
    }

    @Override
    public boolean isHighlightable() {
        return false; // we render the highlight in the scissor render
    }
}
