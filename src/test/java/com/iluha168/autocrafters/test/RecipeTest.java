package com.iluha168.autocrafters.test;

import com.iluha168.autocrafters.block.AutoCutterBlock;
import com.iluha168.autocrafters.block.AutoGrindstoneBlock;
import com.iluha168.autocrafters.block.AutoLoomBlock;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;

public class RecipeTest {
	public static final String TEMPLATE = "autocrafters:recipe_test";

	private void testConversion(GameTestHelper context, Item input, Item output) {
		BlockPos outputPos = new BlockPos(0, 0, 0);
		BlockPos crafterPos = outputPos.above();
		BlockPos powerPos = crafterPos.above();

		context.assertContainerEmpty(outputPos);

		CrafterBlockEntity crafter = context.getBlockEntity(crafterPos, CrafterBlockEntity.class);
		crafter.setItem(4, input.getDefaultInstance());

		context.pulseRedstone(powerPos, 1);
		context.runAfterDelay(10, () -> {
			context.assertContainerContains(outputPos, output);
			context.succeed();
		});
	}

	@GameTest(structure = TEMPLATE)
	public void testCrafter(GameTestHelper context) {
		testConversion(context, Items.CRAFTING_TABLE, Items.CRAFTER);
	}

	@GameTest(structure = TEMPLATE)
	public void testLoom(GameTestHelper context) {
		testConversion(context, Items.LOOM, AutoLoomBlock.ITEM);
	}

	@GameTest(structure = TEMPLATE)
	public void testGrindstone(GameTestHelper context) {
		testConversion(context, Items.GRINDSTONE, AutoGrindstoneBlock.ITEM);
	}

	@GameTest(structure = TEMPLATE)
	public void testStonecutter(GameTestHelper context) {
		testConversion(context, Items.STONECUTTER, AutoCutterBlock.ITEM);
	}
}
