package com.iluha168.autocrafters.block_entity;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.screen_handler.AutoCutterMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class AutoCutterBlockEntity extends BaseAutoBlockEntity {
    public static final int[] ALL_SLOTS = new int[]{0};
    private static final int DATA_RECIPE_INDEX = 0;

    private final ContainerData data = new SimpleContainerData(1);

    public AutoCutterBlockEntity(BlockPos pos, BlockState state) {
        super(ServerMod.AUTOCUTTER_BLOCK_ENTITY, pos, state, ALL_SLOTS.length);
    }

    public AutoCutterBlockEntity(BlockPos pos, BlockState state, Level level) {
        this(pos, state);
        this.setLevel(level);
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        Level level = this.getLevel();
        if (level == null) return false;
        return !getAvailableRecipes(new SingleRecipeInput(stack), level).isEmpty();
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
        return new AutoCutterMenu(syncId, playerInventory, this, this.data);
    }

    public static SelectableRecipe.SingleInputSet<StonecutterRecipe> getAvailableRecipes(SingleRecipeInput input, Level level) {
        return level.recipeAccess().stonecutterRecipes().selectByInput(input.item());
    }

    public static ItemStack craftStatic(SelectableRecipe.SingleInputSet<StonecutterRecipe> availableRecipes, int recipeIndex, Level level) {
        var recipes = availableRecipes.entries();
        if (recipeIndex < 0 || recipeIndex >= recipes.size())
            return ItemStack.EMPTY;
        var selectable = recipes.get(recipeIndex).recipe();
        return selectable.optionDisplay().resolveForFirstStack(SlotDisplayContext.fromLevel(level));
    }

    @Override
    public ItemStack craft() {
        Level level = this.getLevel();
        if (level == null) return ItemStack.EMPTY;
        SingleRecipeInput recipeInput = new SingleRecipeInput(this.getItem(0));
        ItemStack result = craftStatic(getAvailableRecipes(recipeInput, level), this.data.get(DATA_RECIPE_INDEX), level);
        if (!result.isEmpty())
            this.getItem(0).shrink(1);
        return result;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("RecipeIndex", this.data.get(DATA_RECIPE_INDEX));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.data.set(DATA_RECIPE_INDEX, input.getIntOr("RecipeIndex", -1));
    }
}
