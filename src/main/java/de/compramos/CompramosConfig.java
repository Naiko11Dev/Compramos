package de.compramos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CompramosConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("compramos-by-naiko11.json");

	public static boolean traderSoundsEnabled = true;
	public static int traderSoundFrequency = 0;
	public static boolean montoyaOnTraderDespawnEnabled = true;
	public static boolean terreneitorOnDonkeyMountEnabled = true;
	public static boolean caveSoundsEnabled = true;
	public static int caveSoundFrequency = 1;
	public static boolean caveParticlesEnabled = true;

	private CompramosConfig() {
	}

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

	private static boolean getBoolean(JsonObject config, String key, boolean defaultValue) {
		return config.has(key) && config.get(key).isJsonPrimitive()
				&& config.get(key).getAsJsonPrimitive().isBoolean()
				? config.get(key).getAsBoolean()
				: defaultValue;
	}

	private static int getFrequency(JsonObject config, String key, int min, int max, int defaultValue) {
		if (!config.has(key) || !config.get(key).isJsonPrimitive()
				|| !config.get(key).getAsJsonPrimitive().isNumber()) {
			return defaultValue;
		}

		int value = config.get(key).getAsInt();
		return value >= min && value <= max ? value : defaultValue;
	}
}
