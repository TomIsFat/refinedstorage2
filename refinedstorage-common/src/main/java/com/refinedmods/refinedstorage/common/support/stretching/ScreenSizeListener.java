package com.refinedmods.refinedstorage.common.support.stretching;

@FunctionalInterface
public interface ScreenSizeListener {
    void resized(int playerInventoryY, int topYStart, int topYEnd);
}
