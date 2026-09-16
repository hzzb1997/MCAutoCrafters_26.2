package com.iluha168.autocrafters.screens;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.screen_handler.AutoGrindstoneMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AutoGrindstoneScreen extends BaseAutoScreen<AutoGrindstoneMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ServerMod.modId, "textures/gui/container/autogrindstone.png");

    public AutoGrindstoneScreen(AutoGrindstoneMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        if (this.menu.getRedstonePower() == 1)
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.leftPos + 101, this.topPos + 36, (float) this.imageWidth, 40f, 17, 11, 256, 256);
    }
}
