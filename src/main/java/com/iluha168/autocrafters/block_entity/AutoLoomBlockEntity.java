package com.iluha168.autocrafters.block_entity;

import java.util.List;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.screen_handler.AutoLoomMenu;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class AutoLoomBlockEntity extends BaseAutoBlockEntity {
    public static final int[] ALL_SLOTS = new int[]{0, 1, 2};

    // Precise machine work can apply more layers than doing so by hand!
    // The default vanilla value is 6.
    public static final int MAX_DYE_LAYERS = 15;

    private static final int DATA_PATTERN_INDEX = 0;
    private static final int DATA_TRIGGERED = 1;

    private final ContainerData data = new SimpleContainerData(2);
    @Nullable
    private HolderGetter<BannerPattern> bannerPatternLookup = null;

    public AutoLoomBlockEntity(BlockPos pos, BlockState state) {
        super(ServerMod.AUTOLOOM_BLOCK_ENTITY, pos, state, ALL_SLOTS.length);
    }

    public ContainerData getData() {
        return this.data;
    }

    public HolderGetter<BannerPattern> getBannerPatternLookup() {
        if (this.bannerPatternLookup == null && this.getLevel() != null) {
            this.bannerPatternLookup = this.getLevel().registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        }
        return this.bannerPatternLookup;
    }

    /** Applies one more pattern layer to a banner stack. Returns false if the layer cap is reached. */
    public static boolean applyPatternComponent(ItemStack bannerStack, Holder<BannerPattern> pattern, DyeColor dyeColor) {
        BannerPatternLayers layers = bannerStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        if (layers.layers().size() >= MAX_DYE_LAYERS)
            return false;
        bannerStack.set(DataComponents.BANNER_PATTERNS,
            new BannerPatternLayers.Builder().addAll(layers).add(pattern, dyeColor).build());
        return true;
    }

    @Override
    public ItemStack craft() {
        int selectionIndex = this.data.get(DATA_PATTERN_INDEX);
        if (selectionIndex == -1) return ItemStack.EMPTY;
        HolderGetter<BannerPattern> lookup = this.getBannerPatternLookup();
        if (lookup == null) return ItemStack.EMPTY;

        List<Holder<BannerPattern>> patterns = AutoLoomMenu.getPatternsFor(lookup, this.getItem(2));
        if (selectionIndex < 0 || selectionIndex >= patterns.size()) {
            this.data.set(DATA_PATTERN_INDEX, -1);
            return ItemStack.EMPTY;
        }

        ItemStack bannerStack = this.getItem(0);
        ItemStack dyeStack = this.getItem(1);
        if (bannerStack.isEmpty() || dyeStack.isEmpty())
            return ItemStack.EMPTY;

        DyeColor dyeColor = dyeStack.get(DataComponents.DYE);
        if (dyeColor == null) return ItemStack.EMPTY;

        ItemStack outputStack = bannerStack.split(1);
        if (!applyPatternComponent(outputStack, patterns.get(selectionIndex), dyeColor)) {
            bannerStack.grow(1);
            return ItemStack.EMPTY;
        }
        dyeStack.split(1);
        if (dyeStack.isEmpty()) this.setItem(1, ItemStack.EMPTY);
        return outputStack;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("PatternIndex", this.data.get(DATA_PATTERN_INDEX));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.data.set(DATA_PATTERN_INDEX, input.getIntOr("PatternIndex", -1));
    }

    @Override
    public int getComparatorOutput() {
        if (this.data.get(DATA_PATTERN_INDEX) != -1)
            return super.getComparatorOutput();
        return 0;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return switch (slot) {
            case 0 -> stack.getItem() instanceof BannerItem;
            case 1 -> stack.getItem() instanceof DyeItem;
            default -> false;
        };
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new AutoLoomMenu(syncId, playerInventory, this, this.data);
    }

    /** Syncs the triggered data slot from the block state. */
    public void refreshTriggered() {
        if (this.getLevel() != null) {
            boolean triggered = this.getBlockState().getValue(BlockStateProperties.TRIGGERED);
            int value = triggered ? 1 : 0;
            if (this.data.get(DATA_TRIGGERED) != value) this.data.set(DATA_TRIGGERED, value);
        }
    }
}
