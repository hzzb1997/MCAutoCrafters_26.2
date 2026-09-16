package com.iluha168.autocrafters.block;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.block_entity.AutoCutterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AutoCutterBlock extends BaseAutoBlock {
    protected static final VoxelShape SHAPE_UP    = Block.box(0, 0, 0, 16, 9, 16);
    protected static final VoxelShape SHAPE_EAST  = Block.box(16-9, 0, 0, 16, 16, 16);
    protected static final VoxelShape SHAPE_WEST  = Block.box(0, 0, 0, 9, 16, 16);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 0, 16, 16, 9);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 16-9, 16, 16, 16);

    public static final Identifier ID = Identifier.fromNamespaceAndPath(ServerMod.modId, "autocutter");
    public static final ResourceKey<Block> BLOCK_KEY = ResourceKey.create(Registries.BLOCK, ID);
    public static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, ID);

    public static final Block BLOCK = Registry.register(
        BuiltInRegistries.BLOCK,
        BLOCK_KEY,
        new AutoCutterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTER).noOcclusion().setId(BLOCK_KEY))
    );

    public static final Item ITEM = Registry.register(
        BuiltInRegistries.ITEM,
        ITEM_KEY,
        new BlockItem(BLOCK, new Item.Properties().useBlockDescriptionPrefix().setId(ITEM_KEY))
    );

    public AutoCutterBlock(Properties settings){
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutoCutterBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.ORIENTATION)) {
            case EAST_UP, NORTH_UP, SOUTH_UP, WEST_UP -> SHAPE_UP;
            case UP_EAST, DOWN_EAST -> SHAPE_WEST;
            case UP_NORTH, DOWN_NORTH -> SHAPE_SOUTH;
            case UP_SOUTH, DOWN_SOUTH -> SHAPE_NORTH;
            case UP_WEST, DOWN_WEST -> SHAPE_EAST;
        };
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}
