/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Naiko11
 */

package de.compramos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.compramos.sound.CompramosSounds;

/**
 * Initializes the mod and manages its server-side sound and particle behavior.
 */
public final class CompramosMod implements ModInitializer {
	public static final String MOD_ID = "compramos-by-naiko11";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	// Interval bounds are measured in game ticks (20 ticks per second).
	private static final int MIN_SOUND_INTERVAL_TICKS = 1_200;
	private static final int SOUND_INTERVAL_VARIATION_TICKS = 1_201;
	private static final int MIN_CAVE_SOUND_INTERVAL_TICKS = 108_000;
	private static final int CAVE_SOUND_INTERVAL_VARIATION_TICKS = 72_001;
	private static final int CAVE_CHECK_RETRY_TICKS = 600;
	private static final int CAVE_CHECK_RETRY_VARIATION_TICKS = 601;
	private static final Map<UUID, Long> NEXT_TRADER_SOUND_TICKS = new HashMap<>();
	private static final Map<UUID, Long> NEXT_CAVE_SOUND_TICKS = new HashMap<>();
	private static final Map<UUID, UUID> LAST_DONKEY_MOUNTS = new HashMap<>();

	/**
	 * Loads settings, registers sounds, and subscribes to relevant server events.
	 */
	@Override
	public void onInitialize() {
		CompramosConfig.load();
		CompramosSounds.register();
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof WanderingTrader trader) {
				scheduleNextSound(trader, world.getGameTime());
			}
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof WanderingTrader trader) {
				NEXT_TRADER_SOUND_TICKS.remove(trader.getUUID());
				if (trader.getRemovalReason() == Entity.RemovalReason.DISCARDED
						&& CompramosConfig.montoyaOnTraderDespawnEnabled) {
					trader.playSound(CompramosSounds.MONTOYA_POR_FAVOR, CompramosConfig.soundVolume, 1.0f);
				}
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			NEXT_CAVE_SOUND_TICKS.remove(handler.player.getUUID());
			LAST_DONKEY_MOUNTS.remove(handler.player.getUUID());
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				Entity vehicle = player.getVehicle();
				if (vehicle instanceof Donkey donkey) {
					// Remember the current mount so the sound only plays on a new mount.
					UUID previousDonkey = LAST_DONKEY_MOUNTS.put(player.getUUID(), donkey.getUUID());
					if (!donkey.getUUID().equals(previousDonkey)
							&& CompramosConfig.terreneitorOnDonkeyMountEnabled) {
						donkey.playSound(CompramosSounds.TERRENEITOR, CompramosConfig.soundVolume, 1.0f);
					}
				} else {
					LAST_DONKEY_MOUNTS.remove(player.getUUID());
				}
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// Cave checks are intentionally less frequent than the server tick rate.
			if (server.getTickCount() % 100 != 0) {
				return;
			}

			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!CompramosConfig.caveSoundsEnabled) {
					continue;
				}

				long currentTick = player.level().getGameTime();
				boolean inCave = isInDarkCave(player);
				long nextSoundTick = NEXT_CAVE_SOUND_TICKS.computeIfAbsent(
						player.getUUID(),
						uuid -> getNextCaveSoundTick(player, currentTick, inCave)
				);
				if (currentTick < nextSoundTick) {
					continue;
				}

				// Schedule another long interval in caves, or a shorter retry outside them.
				if (inCave) {
					playCaveSound(player);
				}
				NEXT_CAVE_SOUND_TICKS.put(
						player.getUUID(),
						getNextCaveSoundTick(player, currentTick, inCave)
				);
			}
		});
		ServerTickEvents.END_LEVEL_TICK.register(world -> {
			// Trader schedules are checked once per second rather than on every tick.
			if (world.getGameTime() % 20 != 0) {
				return;
			}

			for (WanderingTrader trader : world.getEntities(
					EntityTypeTest.forClass(WanderingTrader.class),
					WanderingTrader::isAlive
			)) {
				if (!CompramosConfig.traderSoundsEnabled) {
					continue;
				}

				long nextSoundTick = NEXT_TRADER_SOUND_TICKS.computeIfAbsent(
						trader.getUUID(),
						uuid -> getNextSoundTick(trader, world.getGameTime())
				);
				if (world.getGameTime() >= nextSoundTick) {
					trader.playSound(
							trader.getRandom().nextBoolean()
									? CompramosSounds.FULL_COMPRAMOS
									: CompramosSounds.SIMPLE_COMPRAMOS,
							CompramosConfig.soundVolume,
							1.0f
					);
					scheduleNextSound(trader, world.getGameTime());
				}
			}
		});

		LOGGER.info("Initializing Compramos mod.");
	}

	/**
	 * Sets the next sound time for a wandering trader.
	 *
	 * @param trader the trader to schedule
	 * @param currentTick the current world tick
	 */
	private static void scheduleNextSound(WanderingTrader trader, long currentTick) {
		NEXT_TRADER_SOUND_TICKS.put(trader.getUUID(), getNextSoundTick(trader, currentTick));
	}

	/**
	 * Clears cached sound times so changed settings are applied to active entities.
	 */
	public static void refreshSoundSchedules() {
		NEXT_TRADER_SOUND_TICKS.clear();
		NEXT_CAVE_SOUND_TICKS.clear();
	}

	/**
	 * Calculates a randomized next sound time using the configured trader frequency.
	 *
	 * @param trader the trader whose random generator is used
	 * @param currentTick the current world tick
	 * @return the world tick when the next sound is due
	 */
	private static long getNextSoundTick(WanderingTrader trader, long currentTick) {
		int frequency = CompramosConfig.traderSoundFrequency;
		int minInterval = MIN_SOUND_INTERVAL_TICKS * (1 << frequency);
		int intervalVariation = SOUND_INTERVAL_VARIATION_TICKS * (1 << frequency);
		return currentTick + minInterval + trader.getRandom().nextInt(intervalVariation);
	}

	/**
	 * Checks whether the player is underground and in sufficiently low light.
	 *
	 * @param player the player to inspect
	 * @return {@code true} if the player's position meets the dark-cave conditions
	 */
	private static boolean isInDarkCave(ServerPlayer player) {
		var level = player.level();
		var position = player.blockPosition();
		return !level.canSeeSkyFromBelowWater(position)
				&& level.getMaxLocalRawBrightness(position) <= 7;
	}

	/**
	 * Sends a cave ambience sound and, when enabled, its particle effect to one player.
	 *
	 * @param player the player who receives the sound and particles
	 */
	private static void playCaveSound(ServerPlayer player) {
		var sound = player.getRandom().nextBoolean()
				? CompramosSounds.FULL_COMPRAMOS
				: CompramosSounds.SIMPLE_COMPRAMOS;
		if (CompramosConfig.caveParticlesEnabled) {
			player.connection.send(new ClientboundLevelParticlesPacket(
					ParticleTypes.ENCHANT,
					false,
					true,
					player.getX(),
					player.getY() + 1.0,
					player.getZ(),
					0.7f,
					0.8f,
					0.7f,
					0.05f,
					8
			));
		}
		player.connection.send(new ClientboundSoundPacket(
				BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
				SoundSource.AMBIENT,
				player.getX(),
				player.getY(),
				player.getZ(),
				0.65f * CompramosConfig.soundVolume,
				0.8f + player.getRandom().nextFloat() * 0.4f,
				player.getRandom().nextLong()
		));
	}

	/**
	 * Calculates the next cave check or sound time according to the player's location.
	 *
	 * @param player the player whose random generator is used
	 * @param currentTick the current world tick
	 * @param inCave whether the player currently meets the dark-cave conditions
	 * @return the world tick when the next check or sound is due
	 */
	private static long getNextCaveSoundTick(ServerPlayer player, long currentTick, boolean inCave) {
		if (inCave) {
			int frequency = CompramosConfig.caveSoundFrequency;
			int minInterval = switch (frequency) {
				case 0 -> 36_000;
				case 2 -> 144_000;
				default -> MIN_CAVE_SOUND_INTERVAL_TICKS;
			};
			int intervalVariation = switch (frequency) {
				case 0 -> 36_001;
				case 2 -> 72_001;
				default -> CAVE_SOUND_INTERVAL_VARIATION_TICKS;
			};
			return currentTick + minInterval + player.getRandom().nextInt(intervalVariation);
		}

		return currentTick + CAVE_CHECK_RETRY_TICKS
				+ player.getRandom().nextInt(CAVE_CHECK_RETRY_VARIATION_TICKS);
	}
}
