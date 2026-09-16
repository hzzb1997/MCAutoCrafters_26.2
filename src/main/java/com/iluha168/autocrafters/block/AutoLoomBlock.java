package com.iluha168.autocrafters.block;

import com.iluha168.autocrafters.ServerMod;
import com.iluha168.autocrafters.block_entity.AutoLoomBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Registry;

public class AutoLoomBlock extends BaseAutoBlock {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ServerMod.modId, "autoloom");
    public static final ResourceKey<Block> BLOCK_KEY = ResourceKey.create(Registries.BLOCK, ID);
    public static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, ID);

    public static final Block BLOCK = Registry.register(
        BuiltInRegistries.BLOCK,
        BLOCK_KEY,
        new AutoLoomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTER).sound(SoundType.WOOD).setId(BLOCK_KEY))
    );

    public static final Item ITEM = Registry.register(
        BuiltInRegistries.ITEM,
        ITEM_KEY,
        new BlockItem(BLOCK, new Item.Properties().useBlockDescriptionPrefix().setId(ITEM_KEY))
    );

    public AutoLoomBlock(Properties settings){
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new AutoLoomBlockEntity(pos, state);
    }
}
