package com.iluha168.autocrafters.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class AutoLoomCraftTest {
	public static final int PULSES = 16 + 5;
	public static final int PULSE_DELAY = 10;

	@GameTest(
		structure = "autocrafters:autoloom_craft_test",
		maxTicks = PULSES * PULSE_DELAY
	)
	public void testLoom(GameTestHelper context) {
		for (int i = 0; i < PULSES; i++) {
			final int tick = i * PULSE_DELAY;
			context.runAtTickTime(tick, () ->
				context.pulseRedstone(new BlockPos(0, 1, 0), 1)
			);
		}

		context.runAtTickTime(PULSES * PULSE_DELAY, () -> {
			BlockPos outputPos = new BlockPos(7, 0, 1);
			ChestBlockEntity output = context.getBlockEntity(outputPos, ChestBlockEntity.class);
			ChestBlockEntity check = context.getBlockEntity(outputPos.south(), ChestBlockEntity.class);

			for (int i = 0; i < check.getContainerSize(); i++) {
				context.assertTrue(
					ItemStack.matches(output.getItem(i), check.getItem(i)),
					"Slots " + i + " in target and check chests are different"
				);
			}

			context.succeed();
		});
	}
}
