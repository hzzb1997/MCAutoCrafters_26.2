package com.iluha168.autocrafters.block_entity;

import com.iluha168.autocrafters.ServerMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AutoFletchingTableBlockEntity extends BaseAutoBlockEntity {
    private static final int[] ALL_SLOTS = new int[0];

    public AutoFletchingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ServerMod.AUTOFLETCHING_BLOCK_ENTITY, pos, state, ALL_SLOTS.length);
    }

    @Override
    public ItemStack craft() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return null;
    }
}
