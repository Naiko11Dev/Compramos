package de.compramos;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CompramosMod implements ModInitializer {
	public static final String MOD_ID = "compramos";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Compramos Mod wird initialisiert.");
	}
}
