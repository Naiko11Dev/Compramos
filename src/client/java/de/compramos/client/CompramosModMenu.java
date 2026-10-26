package de.compramos.client;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import de.compramos.client.gui.CompramosConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public final class CompramosModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return CompramosConfigScreen::new;
	}
}
