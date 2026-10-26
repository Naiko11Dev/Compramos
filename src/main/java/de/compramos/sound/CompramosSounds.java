/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Naiko11
 */

package de.compramos.sound;

import de.compramos.CompramosMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Defines and registers the sound events used by Compramos.
 */
public final class CompramosSounds {
	/** The full version of the Compramos sound. */
	public static final SoundEvent FULL_COMPRAMOS = create("full_compramos");
	/** The shortened version of the Compramos sound. */
	public static final SoundEvent SIMPLE_COMPRAMOS = create("simple_compramos");
	/** The sound played when a wandering trader despawns. */
	public static final SoundEvent MONTOYA_POR_FAVOR = create("montoya_por_favor");
	/** The sound played when mounting a donkey. */
	public static final SoundEvent TERRENEITOR = create("terreneitor");

	private CompramosSounds() {
	}

	/**
	 * Initializes this class so its sound events are registered.
	 */
	public static void register() {
	}

	/**
	 * Creates and registers a sound event under this mod's identifier.
	 *
	 * @param path the sound's path within the mod namespace
	 * @return the registered sound event
	 */
	private static SoundEvent create(String path) {
		Identifier id = Identifier.fromNamespaceAndPath(CompramosMod.MOD_ID, path);
		return Registry.register(
				BuiltInRegistries.SOUND_EVENT,
				id,
				SoundEvent.createVariableRangeEvent(id)
		);
	}
}
