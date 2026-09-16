package com.iluha168.autocrafters.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Shared base for all auto-crafter style block entities.
 */
public abstract class BaseAutoBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    protected final NonNullList<ItemStack> stacks;

    protected BaseAutoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    /** Produce the crafting result for the current inputs (without consuming, unless documented). */
    public abstract ItemStack craft();

    public int getComparatorOutput() {
        int filled = 0;
        for (int i = 0; i < this.getContainerSize(); i++)
            filled += this.getItem(i).isEmpty() ? 0 : 1;
        return filled;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.stacks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.stacks);
    }

    // ---- Container ----

    @Override
    public int getContainerSize() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.stacks, slot, amount);
        if (!stack.isEmpty()) this.setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.stacks, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        stack.limitSize(this.getMaxStackSize(stack));
        this.stacks.set(slot, stack);
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.stacks.clear();
    }

    // ---- WorldlyContainer ----

    @Override
    public abstract int[] getSlotsForFace(Direction side);

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return this.canInsert(slot, stack, side);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return this.canExtract(slot, stack, side);
    }

    /** Whether an item may be inserted into the given slot. */
    public abstract boolean canInsert(int slot, ItemStack stack, @Nullable Direction side);

    /** Whether an item may be extracted from the given slot. */
    public boolean canExtract(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.canInsert(slot, stack, null);
    }

    // ---- MenuProvider ----

    @Override
    public Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public abstract AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player);
}
