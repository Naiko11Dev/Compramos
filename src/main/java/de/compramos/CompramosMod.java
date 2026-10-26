package de.compramos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.compramos.sound.CompramosSounds;

public final class CompramosMod implements ModInitializer {
	public static final String MOD_ID = "compramos-by-naiko11";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final int MIN_SOUND_INTERVAL_TICKS = 1_200;
	private static final int SOUND_INTERVAL_VARIATION_TICKS = 1_201;
	private static final Map<UUID, Long> NEXT_TRADER_SOUND_TICKS = new HashMap<>();

	@Override
	public void onInitialize() {
		CompramosSounds.register();
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof WanderingTrader trader) {
				scheduleNextSound(trader, world.getGameTime());
			}
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof WanderingTrader trader) {
				NEXT_TRADER_SOUND_TICKS.remove(trader.getUUID());
			}
		});
		ServerTickEvents.END_LEVEL_TICK.register(world -> {
			if (world.getGameTime() % 20 != 0) {
				return;
			}

			for (WanderingTrader trader : world.getEntities(
					EntityTypeTest.forClass(WanderingTrader.class),
					WanderingTrader::isAlive
			)) {
				long nextSoundTick = NEXT_TRADER_SOUND_TICKS.computeIfAbsent(
						trader.getUUID(),
						uuid -> getNextSoundTick(trader, world.getGameTime())
				);
				if (world.getGameTime() >= nextSoundTick) {
					trader.playSound(
							trader.getRandom().nextBoolean()
									? CompramosSounds.FULL_COMPRAMOS
									: CompramosSounds.SIMPLE_COMPRAMOS,
							1.0f,
							1.0f
					);
					scheduleNextSound(trader, world.getGameTime());
				}
			}
		});

		LOGGER.info("Initializing Compramos mod.");
	}

	private static void scheduleNextSound(WanderingTrader trader, long currentTick) {
		NEXT_TRADER_SOUND_TICKS.put(trader.getUUID(), getNextSoundTick(trader, currentTick));
	}

	private static long getNextSoundTick(WanderingTrader trader, long currentTick) {
		return currentTick + MIN_SOUND_INTERVAL_TICKS
				+ trader.getRandom().nextInt(SOUND_INTERVAL_VARIATION_TICKS);
	}
}
