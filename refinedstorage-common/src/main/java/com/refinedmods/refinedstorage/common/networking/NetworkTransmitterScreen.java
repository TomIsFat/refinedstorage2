package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING;
import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class NetworkTransmitterScreen extends AbstractBaseScreen<NetworkTransmitterContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/network_transmitter.png");

    private final TransmittingIcon icon;

    public NetworkTransmitterScreen(final NetworkTransmitterContainerMenu menu,
                                    final Inventory playerInventory,
                                    final Component title) {
        super(menu, playerInventory, title);
        this.inventoryLabelY = 42;
        this.imageWidth = 176;
        this.imageHeight = 137;
        this.icon = new TransmittingIcon(isIconActive());
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        icon.tick(isIconActive());
    }

    private boolean isIconActive() {
        return !getMenu().getStatus().error() && getMenu().getStatus().transmitting();
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        icon.render(graphics, leftPos + 29, topPos + 22);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        final NetworkTransmitterData status = getMenu().getStatus();
        final int x = 25 + 4 + icon.getWidth() + 4;
        if (status.error()) {
            graphics.blitSprite(WARNING, x, 23, WARNING_SIZE, WARNING_SIZE);
        }
        graphics.drawString(font, status.message(), x + (status.error() ? (10 + 4) : 0), 25, 4210752, false);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
