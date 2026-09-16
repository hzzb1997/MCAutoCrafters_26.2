package com.iluha168.autocrafters;

import java.util.Set;

import com.iluha168.autocrafters.block.AutoCutterBlock;
import com.iluha168.autocrafters.block.AutoFletchingTableBlock;
import com.iluha168.autocrafters.block.AutoGrindstoneBlock;
import com.iluha168.autocrafters.block.AutoLoomBlock;
import com.iluha168.autocrafters.block_entity.AutoCutterBlockEntity;
import com.iluha168.autocrafters.block_entity.AutoFletchingTableBlockEntity;
import com.iluha168.autocrafters.block_entity.AutoGrindstoneBlockEntity;
import com.iluha168.autocrafters.block_entity.AutoLoomBlockEntity;
import com.iluha168.autocrafters.screen_handler.AutoCutterMenu;
import com.iluha168.autocrafters.screen_handler.AutoGrindstoneMenu;
import com.iluha168.autocrafters.screen_handler.AutoLoomMenu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ServerMod implements ModInitializer {
	public static final String modId = "autocrafters";

	public static final BlockEntityType<AutoLoomBlockEntity> AUTOLOOM_BLOCK_ENTITY = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, AutoLoomBlock.ID,
		new BlockEntityType<>(AutoLoomBlockEntity::new, Set.of(AutoLoomBlock.BLOCK))
	);
	public static final BlockEntityType<AutoGrindstoneBlockEntity> AUTOGRINDSTONE_BLOCK_ENTITY = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, AutoGrindstoneBlock.ID,
		new BlockEntityType<>(AutoGrindstoneBlockEntity::new, Set.of(AutoGrindstoneBlock.BLOCK))
	);
	public static final BlockEntityType<AutoCutterBlockEntity> AUTOCUTTER_BLOCK_ENTITY = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, AutoCutterBlock.ID,
		new BlockEntityType<>(AutoCutterBlockEntity::new, Set.of(AutoCutterBlock.BLOCK))
	);
	public static final BlockEntityType<AutoFletchingTableBlockEntity> AUTOFLETCHING_BLOCK_ENTITY = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, AutoFletchingTableBlock.ID,
		new BlockEntityType<>(AutoFletchingTableBlockEntity::new, Set.of(AutoFletchingTableBlock.BLOCK))
	);

	public static final MenuType<AutoLoomMenu> AUTOLOOM_MENU = Registry.register(
		BuiltInRegistries.MENU, AutoLoomBlock.ID,
		new MenuType<>(AutoLoomMenu::new, FeatureFlags.VANILLA_SET)
	);
	public static final MenuType<AutoGrindstoneMenu> AUTOGRINDSTONE_MENU = Registry.register(
		BuiltInRegistries.MENU, AutoGrindstoneBlock.ID,
		new MenuType<>(AutoGrindstoneMenu::new, FeatureFlags.VANILLA_SET)
	);
	public static final MenuType<AutoCutterMenu> AUTOCUTTER_MENU = Registry.register(
		BuiltInRegistries.MENU, AutoCutterBlock.ID,
		new MenuType<>(AutoCutterMenu::new, FeatureFlags.VANILLA_SET)
	);

	private static final ResourceKey<CreativeModeTab> REDSTONE_TAB =
		ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("redstone_blocks"));

	@Override
	public void onInitialize() {
		CreativeModeTabEvents.modifyOutputEvent(REDSTONE_TAB).register(output ->
			output.insertAfter(Items.CRAFTER,
				AutoLoomBlock.ITEM,
				AutoGrindstoneBlock.ITEM,
				AutoCutterBlock.ITEM,
				AutoFletchingTableBlock.ITEM
			)
		);
	}
}
