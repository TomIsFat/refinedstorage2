package com.refinedmods.refinedstorage.common.storage.diskdrive;

import com.refinedmods.refinedstorage.common.storage.AbstractProgressStorageScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class DiskDriveScreen extends AbstractProgressStorageScreen<DiskDriveContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/disk_drive.png");
    private static final MutableComponent DISKS_TEXT = createTranslation("gui", "disk_drive.disks");

    public DiskDriveScreen(final DiskDriveContainerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title, 99);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, DISKS_TEXT, 60, 42, 4210752, false);
    }
}
