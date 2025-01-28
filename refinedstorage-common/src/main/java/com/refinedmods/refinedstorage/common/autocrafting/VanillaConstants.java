package com.refinedmods.refinedstorage.common.autocrafting;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class VanillaConstants {
    public static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM =
        ResourceLocation.withDefaultNamespace("item/empty_slot_smithing_template_armor_trim");
    public static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE =
        ResourceLocation.withDefaultNamespace("item/empty_slot_smithing_template_netherite_upgrade");
    public static final List<ResourceLocation> EMPTY_SLOT_SMITHING_TEMPLATES =
        List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE);
    public static final Component MISSING_SMITHING_TEMPLATE_TOOLTIP =
        Component.translatable("container.upgrade.missing_template_tooltip");
    public static final ResourceLocation STONECUTTER_RECIPE_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace(
        "container/stonecutter/recipe_selected"
    );
    public static final ResourceLocation STONECUTTER_RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace(
        "container/stonecutter/recipe_highlighted"
    );
    public static final ResourceLocation STONECUTTER_RECIPE_SPRITE = ResourceLocation.withDefaultNamespace(
        "container/stonecutter/recipe"
    );
    public static final Vector3f ARMOR_STAND_TRANSLATION = new Vector3f();
    public static final Quaternionf ARMOR_STAND_ANGLE = (new Quaternionf()).rotationXYZ(0.43633232F, 0.0F, 3.1415927F);
    public static final int STONECUTTER_RECIPES_PER_ROW = 4;
    public static final int STONECUTTER_ROWS_VISIBLE = 3;

    private VanillaConstants() {
    }
}
