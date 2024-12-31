package com.refinedmods.refinedstorage.common.support;

import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class Sprites {
    public static final ResourceLocation LIGHT_ARROW = createIdentifier("light_arrow");
    public static final ResourceLocation SLOT = createIdentifier("slot");
    public static final int LIGHT_ARROW_WIDTH = 22;
    public static final int LIGHT_ARROW_HEIGHT = 15;
    public static final ResourceLocation WARNING = createIdentifier("warning");
    public static final int WARNING_SIZE = 10;
    public static final int ICON_SIZE = 12;
    public static final ResourceLocation ERROR = createIdentifier("error");
    public static final ResourceLocation START = createIdentifier("start");
    public static final ResourceLocation CANCEL = createIdentifier("cancel");
    public static final ResourceLocation RESET = createIdentifier("reset");
    public static final ResourceLocation SET = createIdentifier("set");

    private Sprites() {
    }
}
