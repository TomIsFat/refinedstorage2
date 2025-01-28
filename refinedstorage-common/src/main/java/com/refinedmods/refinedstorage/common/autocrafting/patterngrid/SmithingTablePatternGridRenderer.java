package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.autocrafting.VanillaConstants;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_HEIGHT;
import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_PADDING;
import static com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen.INSET_WIDTH;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

class SmithingTablePatternGridRenderer implements PatternGridRenderer {
    private static final ResourceLocation SPRITE = createIdentifier("pattern_grid/smithing_table");

    private final PatternGridContainerMenu menu;
    private final int leftPos;
    private final int topPos;
    private final int x;
    private final int y;
    private final CyclingSlotBackground templateIcon;
    private final CyclingSlotBackground baseIcon;
    private final CyclingSlotBackground additionalIcon;

    @Nullable
    private ArmorStand preview;
    private ItemStack result = ItemStack.EMPTY;

    SmithingTablePatternGridRenderer(final PatternGridContainerMenu menu,
                                     final int leftPos,
                                     final int topPos,
                                     final int x,
                                     final int y) {
        this.menu = menu;
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.x = x;
        this.y = y;
        this.templateIcon = new CyclingSlotBackground(menu.getFirstSmithingTableSlotIndex());
        this.baseIcon = new CyclingSlotBackground(menu.getFirstSmithingTableSlotIndex() + 1);
        this.additionalIcon = new CyclingSlotBackground(menu.getFirstSmithingTableSlotIndex() + 2);
    }

    @Override
    public void addWidgets(final Consumer<AbstractWidget> widgets,
                           final Consumer<AbstractWidget> renderables) {
        final Level level = ClientPlatformUtil.getClientLevel();
        if (level == null) {
            return;
        }
        preview = new ArmorStand(level, 0.0, 0.0, 0.0);
        preview.setNoBasePlate(true);
        preview.setShowArms(true);
        preview.yBodyRot = 210.0F;
        preview.setXRot(25.0F);
        preview.yHeadRot = preview.getYRot();
        preview.yHeadRotO = preview.getYRot();
        result = menu.getSmithingTableResult().copy();
        updatePreview();
    }

    @Override
    public void tick() {
        final ItemStack currentResult = menu.getSmithingTableResult();
        if (!ItemStack.isSameItemSameComponents(currentResult, result)) {
            result = currentResult.copy();
            updatePreview();
        }
        final Optional<SmithingTemplateItem> templateItem = menu.getSmithingTableTemplateItem();
        templateIcon.tick(VanillaConstants.EMPTY_SLOT_SMITHING_TEMPLATES);
        baseIcon.tick(templateItem.map(SmithingTemplateItem::getBaseSlotEmptyIcons).orElse(List.of()));
        additionalIcon.tick(templateItem.map(SmithingTemplateItem::getAdditionalSlotEmptyIcons).orElse(List.of()));
    }

    @Override
    public int getClearButtonX() {
        return leftPos + 112;
    }

    @Override
    public int getClearButtonY() {
        return y + 26;
    }

    @Override
    public void renderBackground(final GuiGraphics graphics,
                                 final float partialTicks,
                                 final int mouseX,
                                 final int mouseY) {
        graphics.enableScissor(x, y, x + INSET_WIDTH, y + INSET_HEIGHT);
        graphics.blitSprite(SPRITE, x + INSET_PADDING, y + 26, 98, 18);
        renderIcons(graphics, partialTicks);
        if (preview != null) {
            InventoryScreen.renderEntityInInventory(
                graphics,
                x + 128F,
                y + 52F,
                25.0F,
                VanillaConstants.ARMOR_STAND_TRANSLATION,
                VanillaConstants.ARMOR_STAND_ANGLE,
                null,
                preview
            );
        }
        graphics.disableScissor();
    }

    private void renderIcons(final GuiGraphics graphics, final float partialTicks) {
        templateIcon.render(menu, graphics, partialTicks, leftPos, topPos);
        baseIcon.render(menu, graphics, partialTicks, leftPos, topPos);
        additionalIcon.render(menu, graphics, partialTicks, leftPos, topPos);
    }

    @Override
    public void renderTooltip(final Font font,
                              @Nullable final Slot hoveredSlot,
                              final GuiGraphics graphics,
                              final int mouseX,
                              final int mouseY) {
        if (hoveredSlot == null || hoveredSlot.hasItem()) {
            return;
        }
        final int firstSlotIndex = menu.getFirstSmithingTableSlotIndex();
        menu.getSmithingTableTemplateItem().ifPresentOrElse(template -> {
            if (hoveredSlot.index == firstSlotIndex + 1) {
                graphics.renderTooltip(font, split(font, template.getBaseSlotDescription()), mouseX, mouseY);
            } else if (hoveredSlot.index == firstSlotIndex + 2) {
                graphics.renderTooltip(font, split(font, template.getAdditionSlotDescription()), mouseX, mouseY);
            }
        }, () -> {
            if (hoveredSlot.index == firstSlotIndex) {
                graphics.renderTooltip(
                    font,
                    split(font, VanillaConstants.MISSING_SMITHING_TEMPLATE_TOOLTIP),
                    mouseX,
                    mouseY
                );
            }
        });
    }

    private static List<FormattedCharSequence> split(final Font font, final Component template) {
        return font.split(template, 115);
    }

    private void updatePreview() {
        if (preview == null) {
            return;
        }
        for (final EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            preview.setItemSlot(equipmentslot, ItemStack.EMPTY);
        }
        if (result.isEmpty()) {
            return;
        }
        if (result.getItem() instanceof ArmorItem armorItem) {
            preview.setItemSlot(armorItem.getEquipmentSlot(), result);
        } else {
            preview.setItemSlot(EquipmentSlot.OFFHAND, result);
        }
    }
}
