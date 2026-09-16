package com.iluha168.autocrafters.screens;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.screen_handler.AutoCutterMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public class AutoCutterScreen extends BaseAutoScreen<AutoCutterMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ServerMod.modId, "textures/gui/container/autocutter.png");
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final Identifier RECIPE_SELECTED_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final Identifier RECIPE_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final Identifier RECIPE_SPRITE = Identifier.withDefaultNamespace("container/stonecutter/recipe");
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int RECIPE_LIST_COLUMNS = 4;
    private static final int RECIPE_LIST_ROWS = 3;
    private static final int RECIPE_ENTRY_WIDTH = 16;
    private static final int RECIPE_ENTRY_HEIGHT = 18;
    private static final int SCROLLBAR_AREA_HEIGHT = 54;
    private static final int RECIPE_LIST_OFFSET_X = 52;
    private static final int RECIPE_LIST_OFFSET_Y = 14;

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private Item lastRecipeInput = Items.AIR;

    public AutoCutterScreen(AutoCutterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY--;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        Item currentRecipeInput = this.menu.getSlot(0).getItem().getItem();
        if (currentRecipeInput != this.lastRecipeInput) {
            this.menu.updateOutputSlot();
            this.lastRecipeInput = currentRecipeInput;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = this.leftPos + RECIPE_LIST_OFFSET_X;
        int y = this.topPos + RECIPE_LIST_OFFSET_Y;

        int scrollPixel = (int) (41.0f * this.scrollOffs);
        Identifier scroller = this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, scroller, this.leftPos + 119, this.topPos + RECIPE_LIST_OFFSET_Y + scrollPixel, SCROLLER_WIDTH, SCROLLER_HEIGHT);

        SelectableRecipe.SingleInputSet<StonecutterRecipe> recipes = this.menu.getAvailableRecipes();
        ContextMap context = SlotDisplayContext.fromLevel(this.minecraft.level);
        int selectedRecipeIndex = this.menu.getSelectedRecipeIndex();
        for (int i = this.startIndex; i < RECIPE_LIST_COLUMNS * RECIPE_LIST_ROWS && i < recipes.size(); i++) {
            int j = i - this.startIndex;
            int entryX = x + j % RECIPE_LIST_COLUMNS * RECIPE_ENTRY_WIDTH;
            int entryY = y + j / RECIPE_LIST_COLUMNS * RECIPE_ENTRY_HEIGHT + 2;

            Identifier sprite = i == selectedRecipeIndex ? RECIPE_SELECTED_SPRITE
                : mouseX >= entryX && mouseY >= entryY && mouseX < entryX + RECIPE_ENTRY_WIDTH && mouseY < entryY + RECIPE_ENTRY_HEIGHT
                    ? RECIPE_HIGHLIGHTED_SPRITE
                    : RECIPE_SPRITE;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, entryX, entryY - 1, RECIPE_ENTRY_WIDTH, RECIPE_ENTRY_HEIGHT);

            SlotDisplay display = recipes.entries().get(i).recipe().optionDisplay();
            graphics.item(display.resolveForFirstStack(context), entryX, entryY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            double mouseX = event.x();
            double mouseY = event.y();
            int x = this.leftPos + RECIPE_LIST_OFFSET_X;
            int y = this.topPos + RECIPE_LIST_OFFSET_Y;

            for (int recipeIndex = this.startIndex; recipeIndex < this.startIndex + RECIPE_LIST_COLUMNS * RECIPE_LIST_ROWS; recipeIndex++) {
                int visibleRecipeIndex = recipeIndex - this.startIndex;
                double recipeMouseX = mouseX - (double) (x + visibleRecipeIndex % RECIPE_LIST_COLUMNS * RECIPE_ENTRY_WIDTH);
                double recipeMouseY = mouseY - (double) (y + visibleRecipeIndex / RECIPE_LIST_COLUMNS * RECIPE_ENTRY_HEIGHT);
                if (recipeMouseX >= 0 && recipeMouseY >= 0 && recipeMouseX < RECIPE_ENTRY_WIDTH && recipeMouseY < RECIPE_ENTRY_HEIGHT
                    && this.menu.clickMenuButton(this.minecraft.player, recipeIndex)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1));
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, recipeIndex);
                    return true;
                }
            }

            x = this.leftPos + 119;
            y = this.topPos + 9;
            this.scrolling = mouseX >= x && mouseX < x + SCROLLER_WIDTH && mouseY >= y && mouseY < y + SCROLLBAR_AREA_HEIGHT;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.scrolling) {
            int yStart = this.topPos + RECIPE_LIST_OFFSET_Y;
            int yEnd = yStart + SCROLLBAR_AREA_HEIGHT;
            this.scrollOffs = Mth.clamp((float) (event.y() - yStart - 7.5f) / (yEnd - yStart - 15.0f), 0f, 1f);
            this.startIndex = (int) (this.scrollOffs * this.getMaxScroll() + 0.5) * RECIPE_LIST_COLUMNS;
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) return true;
        this.scrollOffs = Mth.clamp(this.scrollOffs - (float) scrollY / (float) maxScroll, 0f, 1f);
        this.startIndex = (int) (this.scrollOffs * maxScroll + 0.5) * RECIPE_LIST_COLUMNS;
        return true;
    }

    private boolean isScrollBarActive() {
        return this.menu.getAvailableRecipeCount() > RECIPE_LIST_COLUMNS * RECIPE_LIST_ROWS;
    }

    private int getMaxScroll() {
        return (this.menu.getAvailableRecipeCount() + RECIPE_LIST_COLUMNS - 1) / RECIPE_LIST_COLUMNS - RECIPE_LIST_ROWS;
    }
}
