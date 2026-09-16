package com.iluha168.autocrafters.screen_handler;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Shared menu logic for the auto-crafters. */
public abstract class BaseAutoMenu extends AbstractContainerMenu {
    protected final net.minecraft.world.Container invInput;
    protected final SimpleContainer invOutput = new SimpleContainer(1);
    protected Slot outputSlot;

    protected BaseAutoMenu(MenuType<?> type, int syncId, Inventory playerInventory, net.minecraft.world.Container inventory) {
        super(type, syncId);
        this.invInput = inventory;
        inventory.startOpen(playerInventory.player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.invInput.stillValid(player);
    }

    public abstract ItemStack getOutputPreview();

    public void updateOutputSlot() {
        this.outputSlot.set(this.getOutputPreview());
    }

    /** Adds the standard 27+9 player inventory slots. */
    protected void addPlayerInventory(Inventory playerInventory) {
        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        Slot slot = this.slots.get(invSlot);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack originalStack = slot.getItem();
        ItemStack newStack = originalStack.copy();
        int inputSize = this.invInput.getContainerSize();
        if (invSlot < inputSize) {
            if (!this.moveItemStackTo(originalStack, inputSize, this.slots.size(), true))
                return ItemStack.EMPTY;
        } else {
            for (int i = 0; i < inputSize; i++) {
                if (this.moveItemStackTo(originalStack, i, i + 1, false))
                    break;
            }
            return ItemStack.EMPTY;
        }

        if (originalStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return newStack;
    }
}
