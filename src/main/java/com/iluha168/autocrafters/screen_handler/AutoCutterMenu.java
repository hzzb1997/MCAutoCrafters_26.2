package com.iluha168.autocrafters.screen_handler;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.block.AutoCutterBlock;
import com.iluha168.autocrafters.block_entity.AutoCutterBlockEntity;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;

public class AutoCutterMenu extends BaseAutoMenu {
    private final Slot inputSlot;
    private final ContainerData data;
    private SelectableRecipe.SingleInputSet<StonecutterRecipe> recipesCache =
        SelectableRecipe.SingleInputSet.empty();

    // Client-side constructor.
    public AutoCutterMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new AutoCutterBlockEntity(
            playerInventory.player.blockPosition(),
            AutoCutterBlock.BLOCK.defaultBlockState(),
            playerInventory.player.level()
        ), new SimpleContainerData(1));
    }

    public AutoCutterMenu(int syncId, Inventory playerInventory, AutoCutterBlockEntity inventory, ContainerData data) {
        super(ServerMod.AUTOCUTTER_MENU, syncId, playerInventory, inventory);
        checkContainerSize(inventory, 1);
        checkContainerDataCount(data, 1);
        this.data = data;
        this.addDataSlots(data);

        this.inputSlot = this.addSlot(new Slot(this.invInput, 0, 20, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ((AutoCutterBlockEntity) inventory).canInsert(0, stack, null);
            }
        });
        this.outputSlot = this.addSlot(new Slot(this.invOutput, 0, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addPlayerInventory(playerInventory);

        this.addSlotListener(new net.minecraft.world.inventory.ContainerListener() {
            @Override
            public void slotChanged(net.minecraft.world.inventory.AbstractContainerMenu menu, int slotId, ItemStack stack) {
                if (slotId < 1) slotsChanged(invInput);
            }

            @Override
            public void dataChanged(net.minecraft.world.inventory.AbstractContainerMenu menu, int property, int value) {
            }
        });
        this.slotsChanged(this.invInput);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.getSelectedRecipeIndex() != id) {
            this.setData(0, id);
            this.updateOutputSlot();
            this.broadcastChanges();
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container inventory) {
        this.updateOutputSlot();
        super.slotsChanged(inventory);
    }

    @Override
    public ItemStack getOutputPreview() {
        Level level = this.getLevel();
        if (level == null) return ItemStack.EMPTY;
        SingleRecipeInput recipeInput = new SingleRecipeInput(this.inputSlot.getItem());
        this.recipesCache = AutoCutterBlockEntity.getAvailableRecipes(recipeInput, level);
        return AutoCutterBlockEntity.craftStatic(this.recipesCache, this.getSelectedRecipeIndex(), level);
    }

    private Level getLevel() {
        return this.invInput instanceof AutoCutterBlockEntity be ? be.getLevel() : null;
    }

    public int getSelectedRecipeIndex() {
        return this.data.get(0);
    }

    public SelectableRecipe.SingleInputSet<StonecutterRecipe> getAvailableRecipes() {
        return this.recipesCache;
    }

    public int getAvailableRecipeCount() {
        return this.recipesCache.size();
    }
}
