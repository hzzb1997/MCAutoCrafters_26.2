package com.iluha168.autocrafters.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class BaseAutoScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private final Identifier backgroundTextureId;

    public BaseAutoScreen(T menu, Inventory inventory, Component title, Identifier backgroundTextureId) {
        super(menu, inventory, title);
        this.backgroundTextureId = backgroundTextureId;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.backgroundTextureId,
            this.leftPos, this.topPos, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title.
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }
}
