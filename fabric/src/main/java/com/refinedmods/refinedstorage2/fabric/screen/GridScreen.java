package com.refinedmods.refinedstorage2.fabric.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.handler.GridScreenHandler;
import com.refinedmods.refinedstorage2.fabric.screen.widget.ScrollbarWidget;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GridScreen extends HandledScreen<GridScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(RefinedStorage2Mod.ID, "textures/gui/grid.png");

    private static final int TOP_HEIGHT = 19;
    private static final int BOTTOM_HEIGHT = 99;

    private ScrollbarWidget scrollbar;

    public GridScreen(GridScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        titleX = 7;
        titleY = 7;
        playerInventoryTitleX = 7;
        playerInventoryTitleY = 75;
        backgroundWidth = 227;
        backgroundHeight = 176;
    }

    @Override
    protected void init() {
        backgroundHeight = TOP_HEIGHT + (getVisibleRows() * 18) + BOTTOM_HEIGHT;
        playerInventoryTitleY = backgroundHeight - BOTTOM_HEIGHT + 4;

        super.init();

        getScreenHandler().addSlots(backgroundHeight - BOTTOM_HEIGHT + 17);

        scrollbar = new ScrollbarWidget(client, x + 174, y + 20, 12, (getVisibleRows() * 18) - 2);

        children.add(scrollbar);
    }

    private int getVisibleRows() {
        int screenSpaceAvailable = height - TOP_HEIGHT - BOTTOM_HEIGHT;
        int maxRows = Integer.MAX_VALUE;

        return Math.max(3, Math.min((screenSpaceAvailable / 18) - 3, maxRows));
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        drawTexture(matrices, x, y, 0, 0, backgroundWidth - 34, TOP_HEIGHT);

        for (int i = 0; i < getVisibleRows(); ++i) {
            int textureY = 37;
            if (i == 0) {
                textureY = 19;
            } else if (i == getVisibleRows() - 1) {
                textureY = 55;
            }

            drawTexture(matrices, x, y + TOP_HEIGHT + (18 * i), 0, textureY, backgroundWidth - 34, 18);
        }

        drawTexture(matrices, x, y + TOP_HEIGHT + (18 * getVisibleRows()), 0, 73, backgroundWidth - 34, BOTTOM_HEIGHT);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTicks);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
        scrollbar.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        scrollbar.mouseMoved(mx, my);

        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        return scrollbar.mouseReleased(mx, my, button) || super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        return this.scrollbar.mouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
    }
}
