/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Naiko11
 */

package de.compramos.client;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import de.compramos.client.gui.CompramosConfigScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * Connects the mod's settings screen to the Mod Menu configuration button.
 */
public final class CompramosModMenu implements ModMenuApi {
	/**
	 * Supplies the settings screen that Mod Menu opens for this mod.
	 *
	 * @return a factory that creates the Compramos settings screen
	 */
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return CompramosConfigScreen::new;
	}
}
