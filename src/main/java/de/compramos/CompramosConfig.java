/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Naiko11
 */

package de.compramos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves the mod's user-configurable sound and particle settings.
 */
public final class CompramosConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("compramos-by-naiko11.json");

	/** Enables periodic sounds from wandering traders. */
	public static boolean traderSoundsEnabled = true;
	/** Selects the wandering trader sound interval (0-2). */
	public static int traderSoundFrequency = 0;
	/** Enables the Montoya sound when a wandering trader is discarded. */
	public static boolean montoyaOnTraderDespawnEnabled = true;
	/** Enables the Terreneitor sound when a player mounts a donkey. */
	public static boolean terreneitorOnDonkeyMountEnabled = true;
	/** Enables ambient sounds for players in dark caves. */
	public static boolean caveSoundsEnabled = true;
	/** Selects the cave sound interval (0-2). */
	public static int caveSoundFrequency = 1;
	/** Enables particles alongside cave sounds. */
	public static boolean caveParticlesEnabled = true;

	private CompramosConfig() {
	}

	/**
	 * Reads settings from disk, creating a default file when one does not exist.
	 */
	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
			JsonObject config = GSON.fromJson(reader, JsonObject.class);
			if (config == null) {
				throw new JsonParseException("Configuration must be a JSON object.");
			}

			// Invalid, missing, or out-of-range values fall back to the defaults below.
			traderSoundsEnabled = getBoolean(config, "traderSoundsEnabled", true);
			traderSoundFrequency = getFrequency(config, "traderSoundFrequency", 0, 2, 0);
			montoyaOnTraderDespawnEnabled = getBoolean(config, "montoyaOnTraderDespawnEnabled", true);
			terreneitorOnDonkeyMountEnabled = getBoolean(config, "terreneitorOnDonkeyMountEnabled", true);
			caveSoundsEnabled = getBoolean(config, "caveSoundsEnabled", true);
			caveSoundFrequency = getFrequency(config, "caveSoundFrequency", 0, 2, 1);
			caveParticlesEnabled = getBoolean(config, "caveParticlesEnabled", true);
		} catch (IOException | JsonParseException exception) {
			CompramosMod.LOGGER.error("Could not read Compramos config at {}.", CONFIG_PATH, exception);
		}
	}

	/**
	 * Writes the current settings to disk and refreshes the active sound schedules.
	 */
	public static void save() {
		JsonObject config = new JsonObject();
		config.addProperty("traderSoundsEnabled", traderSoundsEnabled);
		config.addProperty("traderSoundFrequency", traderSoundFrequency);
		config.addProperty("montoyaOnTraderDespawnEnabled", montoyaOnTraderDespawnEnabled);
		config.addProperty("terreneitorOnDonkeyMountEnabled", terreneitorOnDonkeyMountEnabled);
		config.addProperty("caveSoundsEnabled", caveSoundsEnabled);
		config.addProperty("caveSoundFrequency", caveSoundFrequency);
		config.addProperty("caveParticlesEnabled", caveParticlesEnabled);

		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (var writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
			CompramosMod.refreshSoundSchedules();
		} catch (IOException exception) {
			CompramosMod.LOGGER.error("Could not save Compramos config to {}.", CONFIG_PATH, exception);
		}
	}

	/**
	 * Reads a boolean setting or returns its default when the value is missing or invalid.
	 *
	 * @param config the parsed configuration object
	 * @param key the setting's JSON property name
	 * @param defaultValue the value to use when the property is invalid
	 * @return the stored boolean or the default value
	 */
	private static boolean getBoolean(JsonObject config, String key, boolean defaultValue) {
		return config.has(key) && config.get(key).isJsonPrimitive()
				&& config.get(key).getAsJsonPrimitive().isBoolean()
				? config.get(key).getAsBoolean()
				: defaultValue;
	}

	/**
	 * Reads a bounded integer setting or returns its default when invalid.
	 *
	 * @param config the parsed configuration object
	 * @param key the setting's JSON property name
	 * @param min the minimum accepted value
	 * @param max the maximum accepted value
	 * @param defaultValue the value to use when the property is invalid
	 * @return the stored value if it is within bounds, otherwise the default
	 */
	private static int getFrequency(JsonObject config, String key, int min, int max, int defaultValue) {
		if (!config.has(key) || !config.get(key).isJsonPrimitive()
				|| !config.get(key).getAsJsonPrimitive().isNumber()) {
			return defaultValue;
		}

		int value = config.get(key).getAsInt();
		return value >= min && value <= max ? value : defaultValue;
	}
}
