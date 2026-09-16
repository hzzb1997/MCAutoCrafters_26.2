package com.iluha168.autocrafters.screen_handler;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.block.AutoLoomBlock;
import com.iluha168.autocrafters.block_entity.AutoLoomBlockEntity;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;

public class AutoLoomMenu extends BaseAutoMenu {
    private final Slot bannerSlot;
    private final Slot dyeSlot;
    private final Slot patternSlot;
    private final Slot resultSlot;
    private final ContainerData data;

    // Client-side constructor.
    public AutoLoomMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, clientInventory(playerInventory), new SimpleContainerData(2));
    }

    private static AutoLoomBlockEntity clientInventory(Inventory playerInventory) {
        AutoLoomBlockEntity be = new AutoLoomBlockEntity(
            playerInventory.player.blockPosition(),
            AutoLoomBlock.BLOCK.defaultBlockState()
        );
        be.setLevel(playerInventory.player.level());
        return be;
    }

    public AutoLoomMenu(int syncId, Inventory playerInventory, AutoLoomBlockEntity inventory, ContainerData data) {
        super(ServerMod.AUTOLOOM_MENU, syncId, playerInventory, inventory);
        checkContainerSize(inventory, 3);
        checkContainerDataCount(data, 2);
        this.data = data;
        this.addDataSlots(data);

        this.bannerSlot = this.addSlot(new Slot(this.invInput, 0, 13, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BannerItem;
            }
        });
        this.dyeSlot = this.addSlot(new Slot(this.invInput, 1, 33, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DyeItem;
            }
        });
        this.patternSlot = this.addSlot(new Slot(this.invInput, 2, 23, 45) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(DataComponents.PROVIDES_BANNER_PATTERNS);
            }
        });
        this.outputSlot = this.addSlot(new Slot(this.invOutput, 0, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.resultSlot = this.outputSlot;

        this.addPlayerInventory(playerInventory);

        this.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(net.minecraft.world.inventory.AbstractContainerMenu menu, int slotId, ItemStack stack) {
                if (slotId < 3) slotsChanged(null);
            }

            @Override
            public void dataChanged(net.minecraft.world.inventory.AbstractContainerMenu menu, int property, int value) {
            }
        });
        this.slotsChanged(inventory);
    }

    /** Patterns selectable given the item in the pattern slot. */
    public static List<Holder<BannerPattern>> getPatternsFor(HolderGetter<BannerPattern> lookup, ItemStack patternStack) {
        if (lookup == null) return ImmutableList.of();
        if (patternStack.isEmpty()) {
            return lookup.get(BannerPatternTags.NO_ITEM_REQUIRED)
                .map(HolderSet.Named::stream)
                .map(s -> (List<Holder<BannerPattern>>) s.collect(ImmutableList.toImmutableList()))
                .orElse(ImmutableList.of());
        }
        HolderSet<BannerPattern> provided = patternStack.get(DataComponents.PROVIDES_BANNER_PATTERNS);
        if (provided == null) return ImmutableList.of();
        return ImmutableList.copyOf(provided);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.isPatternIndexValid(id) && this.getSelectedPatternIndex() != id) {
            this.setData(0, id);
            this.updateOutputSlot();
            this.broadcastChanges();
            return true;
        }
        return false;
    }

    private boolean isPatternIndexValid(int index) {
        return index >= 0 && index < this.getPatterns().size();
    }

    public List<Holder<BannerPattern>> getPatterns() {
        if (this.invInput instanceof AutoLoomBlockEntity be) {
            return getPatternsFor(be.getBannerPatternLookup(), this.patternSlot.getItem());
        }
        return ImmutableList.of();
    }

    @Override
    public void slotsChanged(Container inventory) {
        List<Holder<BannerPattern>> patterns = this.getPatterns();
        int selectedPatternIndex = this.getSelectedPatternIndex();
        if (patterns.size() == 1) {
            // Auto-select the only pattern available.
            this.setData(0, 0);
        } else if (!this.isPatternIndexValid(selectedPatternIndex)) {
            this.setData(0, -1);
        }
        if (this.bannerSlot.getItem().isEmpty() || this.dyeSlot.getItem().isEmpty())
            this.outputSlot.set(ItemStack.EMPTY);
        else this.updateOutputSlot();
        super.slotsChanged(inventory);
    }

    @Override
    public ItemStack getOutputPreview() {
        List<Holder<BannerPattern>> patterns = this.getPatterns();
        int idx = this.getSelectedPatternIndex();
        if (idx < 0 || idx >= patterns.size()) return ItemStack.EMPTY;
        ItemStack bannerStack = this.bannerSlot.getItem();
        ItemStack dyeStack = this.dyeSlot.getItem();
        if (bannerStack.isEmpty() || dyeStack.isEmpty()) return ItemStack.EMPTY;
        DyeColor dyeColor = dyeStack.get(DataComponents.DYE);
        if (dyeColor == null) return ItemStack.EMPTY;
        ItemStack outputStack = bannerStack.copyWithCount(1);
        if (!AutoLoomBlockEntity.applyPatternComponent(outputStack, patterns.get(idx), dyeColor))
            return ItemStack.EMPTY;
        return outputStack;
    }

    public int getSelectedPatternIndex() {
        return this.data.get(0);
    }

    /** Vanilla-compatible alias. */
    public int getSelectedBannerPatternIndex() {
        return this.getSelectedPatternIndex();
    }

    public List<Holder<BannerPattern>> getSelectablePatterns() {
        return this.getPatterns();
    }

    public void registerUpdateListener(Runnable listener) {
        this.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(net.minecraft.world.inventory.AbstractContainerMenu menu, int slotId, ItemStack stack) {
                listener.run();
            }

            @Override
            public void dataChanged(net.minecraft.world.inventory.AbstractContainerMenu menu, int property, int value) {
                listener.run();
            }
        });
    }

    public int getRedstonePower() {
        return this.data.get(1);
    }

    public Slot getBannerSlot() {
        return this.bannerSlot;
    }

    public Slot getDyeSlot() {
        return this.dyeSlot;
    }

    public Slot getPatternSlot() {
        return this.patternSlot;
    }

    public Slot getResultSlot() {
        return this.resultSlot;
    }
}
