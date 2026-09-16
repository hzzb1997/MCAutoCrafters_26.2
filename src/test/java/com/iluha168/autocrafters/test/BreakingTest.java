package com.iluha168.autocrafters.test;

import com.iluha168.autocrafters.block.AutoCutterBlock;
import com.iluha168.autocrafters.block.AutoGrindstoneBlock;
import com.iluha168.autocrafters.block.AutoLoomBlock;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class BreakingTest {
	public static final String TEMPLATE = "autocrafters:thin_box";

	private void testBreaking(GameTestHelper context, Item autocrafter) {
		context.setBlock(BlockPos.ZERO, ((BlockItem) autocrafter).getBlock());
		context.getLevel().destroyBlock(
			context.absolutePos(BlockPos.ZERO),
			true
		);

		context.assertItemEntityPresent(autocrafter);
		context.succeed();
	}

	@GameTest(structure = TEMPLATE)
	public void testLoom(GameTestHelper context) {
		testBreaking(context, AutoLoomBlock.ITEM);
	}

	@GameTest(structure = TEMPLATE)
	public void testGrindstone(GameTestHelper context) {
		testBreaking(context, AutoGrindstoneBlock.ITEM);
	}

	@GameTest(structure = TEMPLATE)
	public void testStonecutter(GameTestHelper context) {
		testBreaking(context, AutoCutterBlock.ITEM);
	}
}
