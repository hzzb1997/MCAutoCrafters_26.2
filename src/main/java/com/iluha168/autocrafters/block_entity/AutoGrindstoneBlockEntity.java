package com.iluha168.autocrafters.block_entity;

import java.util.UUID;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.screen_handler.AutoGrindstoneMenu;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class AutoGrindstoneBlockEntity extends BaseAutoBlockEntity {
    public static final int[] ALL_SLOTS = new int[]{0, 1};

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            Level level = AutoGrindstoneBlockEntity.this.getLevel();
            if (level == null) return 0;
            return AutoGrindstoneBlockEntity.this.getBlockState().getValue(BlockStateProperties.TRIGGERED) ? 1 : 0;
        }

        @Override
        public void set(int index, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public AutoGrindstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ServerMod.AUTOGRINDSTONE_BLOCK_ENTITY, pos, state, ALL_SLOTS.length);
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return (stack.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments(stack))
            && (slot == 0 || ItemStack.isSameItem(this.getItem(0), stack));
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    public ContainerData getData() {
        return this.data;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new AutoGrindstoneMenu(syncId, playerInventory, this, this.data);
    }

    /** Builds a throwaway GrindstoneMenu driven by a fake player so we can reuse vanilla repair logic. */
    @Nullable
    public GrindstoneMenu constructVirtualMenu() {
        Level level = this.getLevel();
        if (level == null) return null;
        Player virtualPlayer = new Player(level, new GameProfile(new UUID(0, 0), "AutoCrafters")) {
            @Override
            public GameType gameMode() {
                return GameType.SURVIVAL;
            }

            @Override
            public ResolvableProfile getProfile() {
                return ResolvableProfile.createUnresolved(new UUID(0, 0));
            }
        };
        Inventory virtualInventory = new Inventory(virtualPlayer, new EntityEquipment());
        GrindstoneMenu menu = new GrindstoneMenu(
            -1, virtualInventory,
            ContainerLevelAccess.create(level, this.getBlockPos().relative(
                this.getBlockState().getValue(BlockStateProperties.ORIENTATION).front()))
        );
        for (int slot : ALL_SLOTS)
            menu.getSlot(slot).set(this.getItem(slot));
        return menu;
    }

    @Override
    public ItemStack craft() {
        GrindstoneMenu menu = this.constructVirtualMenu();
        if (menu == null) return ItemStack.EMPTY;
        menu.clicked(2, 0, ContainerInput.PICKUP, this.getFakePlayer(menu));
        for (int slot : ALL_SLOTS)
            this.stacks.set(slot, menu.getSlot(slot).getItem());
        return menu.getCarried().copyAndClear();
    }

    private Player getFakePlayer(GrindstoneMenu menu) {
        // Slot 3 always belongs to the menu's player inventory; reuse its owner.
        return ((Inventory) menu.getSlot(3).container).player;
    }
}
