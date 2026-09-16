package com.iluha168.autocrafters;

import com.iluha168.autocrafters.screens.AutoCutterScreen;
import com.iluha168.autocrafters.screens.AutoGrindstoneScreen;
import com.iluha168.autocrafters.screens.AutoLoomScreen;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class ClientMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ServerMod.AUTOLOOM_MENU, AutoLoomScreen::new);
		MenuScreens.register(ServerMod.AUTOGRINDSTONE_MENU, AutoGrindstoneScreen::new);
		MenuScreens.register(ServerMod.AUTOCUTTER_MENU, AutoCutterScreen::new);
	}
}
