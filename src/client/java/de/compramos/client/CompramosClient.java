package de.compramos.client;

import de.compramos.CompramosMod;
import net.fabricmc.api.ClientModInitializer;

public final class CompramosClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CompramosMod.LOGGER.info("Compramos Client wird initialisiert.");
	}
}
