package com.iluha168.autocrafters.block;

import com.iluha168.autocrafters.block_entity.BaseAutoBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class BaseAutoBlock extends CrafterBlock {
    public BaseAutoBlock(Properties settings){
        super(settings);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(BlockStateProperties.CRAFTING)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.CRAFTING, false), 2);
            return;
        }
        super.tick(state, level, pos, random);
    }

    @Override
    protected void dispenseFrom(BlockState state, ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BaseAutoBlockEntity auto)) return;

        ItemStack outputStack = auto.craft();
        if (outputStack.isEmpty()) {
            level.levelEvent(LevelEvent.SOUND_CRAFTER_FAIL, pos, 0);
            return;
        }

        level.setBlock(pos, state.setValue(BlockStateProperties.CRAFTING, true), 2);
        level.scheduleTick(pos, this, 6);
        outputStack.onCraftedBySystem(level);

        Direction side = state.getValue(BlockStateProperties.ORIENTATION).front();

        // Try to push the output into the container in front of us.
        Storage<ItemVariant> remoteInventory = ItemStorage.SIDED.find(level, pos.relative(side), side.getOpposite());
        if (remoteInventory != null) {
            ItemVariant outputVariant = ItemVariant.of(outputStack);
            try (Transaction transaction = Transaction.openOuter()) {
                outputStack.shrink((int) remoteInventory.insert(outputVariant, outputStack.getCount(), transaction));
                transaction.commit();
            }
        }

        // Anything left over is thrown out into the world.
        if (!outputStack.isEmpty()) {
            level.levelEvent(LevelEvent.SOUND_CRAFTER_CRAFT, pos, 0);
            DefaultDispenseItemBehavior.spawnItem(level, outputStack, 6, side, Vec3.atCenterOf(pos).relative(side, 0.7d));
        } else {
            level.levelEvent(LevelEvent.SOUND_CRAFTER_CRAFT, pos, 0);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseAutoBlockEntity auto && auto.createMenu(0, player.getInventory(), player) != null) {
            player.openMenu(auto);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseAutoBlockEntity auto) {
            return auto.getComparatorOutput();
        }
        return 0;
    }
}
