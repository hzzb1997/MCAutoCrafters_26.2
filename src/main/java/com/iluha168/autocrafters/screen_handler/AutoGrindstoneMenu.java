package com.iluha168.autocrafters.screen_handler;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.block.AutoGrindstoneBlock;
import com.iluha168.autocrafters.block_entity.AutoGrindstoneBlockEntity;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AutoGrindstoneMenu extends BaseAutoMenu {
    private final ContainerData data;

    // Client-side constructor.
    public AutoGrindstoneMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, clientInventory(playerInventory), new SimpleContainerData(1));
    }

    private static AutoGrindstoneBlockEntity clientInventory(Inventory playerInventory) {
        AutoGrindstoneBlockEntity be = new AutoGrindstoneBlockEntity(
            playerInventory.player.blockPosition(),
            AutoGrindstoneBlock.BLOCK.defaultBlockState()
        );
        be.setLevel(playerInventory.player.level());
        return be;
    }

    public AutoGrindstoneMenu(int syncId, Inventory playerInventory, AutoGrindstoneBlockEntity inventory, ContainerData data) {
        super(ServerMod.AUTOGRINDSTONE_MENU, syncId, playerInventory, inventory);
        checkContainerSize(inventory, 2);
        checkContainerDataCount(data, 1);
        this.data = data;
        this.addDataSlots(data);

        class GrindSlot extends Slot {
            public GrindSlot(Container container, int index, int x, int y) {
                super(container, index, x, y);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return container instanceof AutoGrindstoneBlockEntity be && be.canInsert(getContainerSlot(), stack, null);
            }
        }

        this.addSlot(new GrindSlot(this.invInput, 0, 49, 19));
        this.addSlot(new GrindSlot(this.invInput, 1, 49, 40));
        this.outputSlot = this.addSlot(new Slot(this.invOutput, 0, 129, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addPlayerInventory(playerInventory);

        this.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(net.minecraft.world.inventory.AbstractContainerMenu menu, int slotId, ItemStack stack) {
                if (slotId < 2) slotsChanged(null);
            }

            @Override
            public void dataChanged(net.minecraft.world.inventory.AbstractContainerMenu menu, int property, int value) {
            }
        });
        this.slotsChanged(inventory);
    }

    @Override
    public ItemStack getOutputPreview() {
        AutoGrindstoneBlockEntity grindstone = (AutoGrindstoneBlockEntity) this.invInput;
        GrindstoneMenu menu = grindstone.constructVirtualMenu();
        return menu == null ? ItemStack.EMPTY : menu.getSlot(2).getItem();
    }

    @Override
    public void slotsChanged(Container inventory) {
        this.updateOutputSlot();
        super.slotsChanged(inventory);
    }

    public int getRedstonePower() {
        return this.data.get(0);
    }
}
