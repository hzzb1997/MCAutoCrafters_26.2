package com.iluha168.autocrafters.screens;

import java.util.List;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.block_entity.AutoLoomBlockEntity;
import com.iluha168.autocrafters.screen_handler.AutoLoomMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class AutoLoomScreen extends BaseAutoScreen<AutoLoomMenu> {
    private static final Identifier BG_LOCATION = Identifier.fromNamespaceAndPath(ServerMod.modId, "textures/gui/container/autoloom.png");
    private static final Identifier BANNER_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/banner");
    private static final Identifier DYE_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/dye");
    private static final Identifier PATTERN_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/banner_pattern");
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/loom/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/loom/scroller_disabled");
    private static final Identifier PATTERN_SELECTED_SPRITE = Identifier.withDefaultNamespace("container/loom/pattern_selected");
    private static final Identifier PATTERN_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("container/loom/pattern_highlighted");
    private static final Identifier PATTERN_SPRITE = Identifier.withDefaultNamespace("container/loom/pattern");
    private static final Identifier ERROR_SPRITE = Identifier.withDefaultNamespace("container/loom/error");

    private static final int PATTERN_COLUMNS = 4;
    private static final int PATTERN_ROWS = 4;
    private static final int ENTRY_SIZE = 14;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int SCROLLER_FULL_HEIGHT = 56;
    private static final int PATTERNS_X = 60;
    private static final int PATTERNS_Y = 13;

    private BannerFlagModel flag;
    private BannerPatternLayers resultBannerPatterns;
    private ItemStack bannerStack = ItemStack.EMPTY;
    private ItemStack dyeStack = ItemStack.EMPTY;
    private ItemStack patternStack = ItemStack.EMPTY;
    private boolean displayPatterns;
    private boolean hasMaxPatterns;
    private float scrollOffs;
    private boolean scrolling;
    private int startRow;

    public AutoLoomScreen(AutoLoomMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BG_LOCATION);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY -= 2;
        this.flag = new BannerFlagModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        this.containerChanged();
        this.menu.registerUpdateListener(this::containerChanged);
    }

    private void containerChanged() {
        this.resultBannerPatterns = null;
        ItemStack result = this.menu.getSlot(3).getItem();
        if (result.getItem() instanceof BannerItem) {
            this.resultBannerPatterns = result.get(DataComponents.BANNER_PATTERNS);
        }

        this.bannerStack = this.menu.getSlot(0).getItem();
        this.dyeStack = this.menu.getSlot(1).getItem();
        this.patternStack = this.menu.getSlot(2).getItem();
        this.displayPatterns = !this.bannerStack.isEmpty() && !this.dyeStack.isEmpty();
        this.hasMaxPatterns = this.resultBannerPatterns != null
            && this.resultBannerPatterns.layers().size() >= AutoLoomBlockEntity.MAX_DYE_LAYERS;
    }

    private int totalRowCount() {
        return Mth.positiveCeilDiv(this.menu.getSelectablePatterns().size(), PATTERN_COLUMNS);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        if (this.menu.getBannerSlot().getItem().isEmpty())
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BANNER_SLOT_SPRITE, this.leftPos + this.menu.getBannerSlot().x, this.topPos + this.menu.getBannerSlot().y, 16, 16);
        if (this.menu.getDyeSlot().getItem().isEmpty())
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DYE_SLOT_SPRITE, this.leftPos + this.menu.getDyeSlot().x, this.topPos + this.menu.getDyeSlot().y, 16, 16);
        if (this.menu.getPatternSlot().getItem().isEmpty())
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PATTERN_SLOT_SPRITE, this.leftPos + this.menu.getPatternSlot().x, this.topPos + this.menu.getPatternSlot().y, 16, 16);

        int scrollerY = this.topPos + PATTERNS_Y + (int) (41.0f * this.scrollOffs);
        Identifier scrollerSprite = this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, scrollerSprite, this.leftPos + 119, scrollerY, SCROLLER_WIDTH, SCROLLER_HEIGHT);

        if (this.resultBannerPatterns != null && !this.hasMaxPatterns) {
            DyeColor color = ((BannerItem) this.menu.getResultSlot().getItem().getItem()).getColor();
            int x = this.leftPos + 141;
            int y = this.topPos + 8;
            graphics.bannerPattern(this.flag, color, this.resultBannerPatterns, x, y, x + 20, y + 40);
        } else if (this.hasMaxPatterns) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE,
                this.menu.getResultSlot().x + this.leftPos - 5, this.menu.getResultSlot().y + this.topPos - 5, 26, 26);
        }

        if (this.displayPatterns) {
            int pX = this.leftPos + PATTERNS_X;
            int pY = this.topPos + PATTERNS_Y;
            List<Holder<BannerPattern>> patterns = this.menu.getSelectablePatterns();
            for (int row = 0; row < PATTERN_ROWS; row++) {
                for (int col = 0; col < PATTERN_COLUMNS; col++) {
                    int index = (this.startRow + row) * PATTERN_COLUMNS + col;
                    if (index >= patterns.size())
                        return;
                    int x = pX + col * ENTRY_SIZE;
                    int y = pY + row * ENTRY_SIZE;
                    boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + ENTRY_SIZE && mouseY < y + ENTRY_SIZE;
                    Identifier sprite;
                    if (index == this.menu.getSelectedPatternIndex()) {
                        sprite = PATTERN_SELECTED_SPRITE;
                    } else if (hovered) {
                        sprite = PATTERN_HIGHLIGHTED_SPRITE;
                        DyeColor dyeColor = this.dyeStack.getOrDefault(DataComponents.DYE, DyeColor.WHITE);
                        String key = patterns.get(index).value().translationKey() + "." + dyeColor.getName();
                        graphics.setTooltipForNextFrame(Component.translatable(key), mouseX, mouseY);
                        graphics.requestCursor(com.mojang.blaze3d.platform.cursor.CursorTypes.POINTING_HAND);
                    } else {
                        sprite = PATTERN_SPRITE;
                    }
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, ENTRY_SIZE, ENTRY_SIZE);
                    TextureAtlasSprite atlasSprite = graphics.getSprite(Sheets.getBannerSprite(patterns.get(index)));
                    this.extractBannerOnButton(graphics, x, y, atlasSprite);
                }
            }
        }
    }

    private void extractBannerOnButton(GuiGraphicsExtractor graphics, int x, int y, TextureAtlasSprite sprite) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 4, y + 2);
        float u0 = sprite.getU0();
        float u1 = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * 21.0f / 64.0f;
        float v0 = sprite.getV0();
        float v1 = v0 + (sprite.getV1() - v0) / 64.0f;
        float v2 = v1 + (sprite.getV1() - v0) * 40.0f / 64.0f;
        graphics.fill(0, 0, 5, 10, DyeColor.GRAY.getTextureDiffuseColor());
        graphics.blit(sprite.atlasLocation(), 0, 0, 5, 10, u0, u1, v1, v2);
        graphics.pose().popMatrix();
    }

    private boolean isScrollBarActive() {
        return this.displayPatterns && this.menu.getSelectablePatterns().size() > PATTERN_COLUMNS * PATTERN_ROWS;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (this.displayPatterns) {
            double mouseX = event.x();
            double mouseY = event.y();
            int pX = this.leftPos + PATTERNS_X;
            int pY = this.topPos + PATTERNS_Y;
            for (int row = 0; row < PATTERN_ROWS; row++) {
                for (int col = 0; col < PATTERN_COLUMNS; col++) {
                    double dx = mouseX - (double) (pX + col * ENTRY_SIZE);
                    double dy = mouseY - (double) (pY + row * ENTRY_SIZE);
                    int index = (this.startRow + row) * PATTERN_COLUMNS + col;
                    if (dx >= 0 && dy >= 0 && dx < ENTRY_SIZE && dy < ENTRY_SIZE
                        && this.menu.clickMenuButton(Minecraft.getInstance().player, index)) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0f));
                        Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, index);
                        return true;
                    }
                }
            }
            if (mouseX >= this.leftPos + 119 && mouseX < this.leftPos + 119 + SCROLLER_WIDTH
                && mouseY >= this.topPos + 9 && mouseY < this.topPos + 9 + SCROLLER_FULL_HEIGHT) {
                this.scrolling = true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        int scrollableRows = this.totalRowCount() - PATTERN_ROWS;
        if (this.scrolling && this.displayPatterns && scrollableRows > 0) {
            int startY = this.topPos + PATTERNS_Y;
            int endY = startY + SCROLLER_FULL_HEIGHT;
            this.scrollOffs = Mth.clamp((float) (event.y() - startY - 7.5f) / (endY - startY - SCROLLER_HEIGHT), 0f, 1f);
            this.startRow = Math.max((int) (this.scrollOffs * scrollableRows + 0.5), 0);
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int scrollableRows = this.totalRowCount() - PATTERN_ROWS;
        if (scrollableRows > 0) {
            this.scrollOffs = Mth.clamp(this.scrollOffs - (float) scrollY / (float) scrollableRows, 0f, 1f);
            this.startRow = Math.max((int) (this.scrollOffs * scrollableRows + 0.5f), 0);
        }
        return true;
    }
}
