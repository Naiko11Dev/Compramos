/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Naiko11
 */

package de.compramos;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap;
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
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("compramos")
					.then(Commands.literal("play")
							.requires(source -> source.permissions()
									.hasPermission(Permissions.COMMANDS_GAMEMASTER))
							.then(Commands.argument("player", EntityArgument.player())
									.executes(CompramosMod::playRandomSound)
									.then(Commands.literal("full").executes(context ->
											playSoundForTarget(context, CompramosSounds.FULL_COMPRAMOS, "the full sound")))
									.then(Commands.literal("simple").executes(context ->
											playSoundForTarget(context, CompramosSounds.SIMPLE_COMPRAMOS, "the simple sound")))
									.then(Commands.literal("montoya").executes(context ->
											playSoundForTarget(context, CompramosSounds.MONTOYA_POR_FAVOR, "the Montoya sound")))
									.then(Commands.literal("terreneitor").executes(context ->
											playSoundForTarget(context, CompramosSounds.TERRENEITOR, "the Terreneitor sound")))))
					.then(Commands.literal("trader")
							.then(Commands.literal("spawn")
									.requires(source -> source.permissions()
											.hasPermission(Permissions.COMMANDS_GAMEMASTER))
									.executes(CompramosMod::spawnTraderAtSource)
									.then(Commands.argument("player", EntityArgument.player())
											.executes(CompramosMod::spawnTraderNearPlayer))))
					.then(Commands.literal("help").executes(context -> {
						context.getSource().sendSuccess(() -> Component.literal(
								"Compramos commands:\n"
										+ "/compramos play <player> [full|simple|montoya|terreneitor]\n"
										+ "/compramos trader spawn [player]\n"
										+ "/compramos help\n"
										+ "Client only: /compramos test <full|simple|montoya|terreneitor>"
						), false);
						return 1;
					})));
		});
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

	private static int playRandomSound(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		SoundEvent sound = player.getRandom().nextBoolean()
				? CompramosSounds.FULL_COMPRAMOS
				: CompramosSounds.SIMPLE_COMPRAMOS;
		return playSoundForTarget(context, sound, "a Compramos sound");
	}

	private static int playSoundForTarget(
			CommandContext<CommandSourceStack> context,
			SoundEvent sound,
			String soundName
	) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		player.connection.send(new ClientboundSoundPacket(
				BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
				SoundSource.PLAYERS,
				player.getX(),
				player.getY(),
				player.getZ(),
				CompramosConfig.soundVolume,
				1.0f,
				player.getRandom().nextLong()
		));
		context.getSource().sendSuccess(
				() -> Component.literal("Playing " + soundName + " for " + player.getScoreboardName() + "."),
				false
		);
		return 1;
	}

	private static int spawnTraderAtSource(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		BlockPos position = BlockPos.containing(source.getPosition());
		return spawnTraderWithFeedback(source, source.getLevel(), position, "the command location");
	}

	private static int spawnTraderNearPlayer(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		return spawnTraderWithFeedback(
				context.getSource(),
				player.level(),
				player.blockPosition(),
				player.getScoreboardName()
		);
	}

	private static int spawnTraderWithFeedback(
			CommandSourceStack source,
			ServerLevel level,
			BlockPos position,
			String location
	) {
		if (!spawnWanderingTrader(level, position, level.getRandom())) {
			source.sendFailure(Component.literal("Could not find a safe place to spawn a Wandering Trader."));
			return 0;
		}

		source.sendSuccess(
				() -> Component.literal("Spawned a Wandering Trader near " + location + "."),
				false
		);
		return 1;
	}

	/**
	 * Tries to spawn a wandering trader near a position.
	 *
	 * @param level the world in which to spawn the trader
	 * @param center the position around which to search for a spawn location
	 * @param random the random source used to choose the spawn position
	 * @return whether the trader was successfully spawned
	 */
	private static boolean spawnWanderingTrader(ServerLevel level, BlockPos center, RandomSource random) {
		if (isInDarkCave(level, center)) {
			for (int attempt = 0; attempt < 40; attempt++) {
				BlockPos spawnPosition = center.offset(
						random.nextInt(17) - 8,
						random.nextInt(9) - 4,
						random.nextInt(17) - 8
				);
				BlockPos floorPosition = spawnPosition.below();
				if (!isInDarkCave(level, spawnPosition)
						|| !level.getBlockState(floorPosition)
								.isFaceSturdy(level, floorPosition, Direction.UP)) {
					continue;
				}
				if (spawnWanderingTraderAt(level, spawnPosition, random)) {
					return true;
				}
			}
			return false;
		}

		for (int attempt = 0; attempt < 10; attempt++) {
			BlockPos spawnPosition = level.getHeightmapPos(
					Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
					center.offset(
							random.nextInt(17) - 8,
							0,
							random.nextInt(17) - 8
					)
			);
			if (spawnWanderingTraderAt(level, spawnPosition, random)) {
				return true;
			}
		}
		return false;
	}

	private static boolean spawnWanderingTraderAt(ServerLevel level, BlockPos position, RandomSource random) {
		Entity spawnedEntity = BuiltInRegistries.ENTITY_TYPE
				.getValue(Identifier.withDefaultNamespace("wandering_trader"))
				.create(level, EntitySpawnReason.EVENT);
		if (!(spawnedEntity instanceof WanderingTrader spawnedTrader)) {
			return false;
		}

		spawnedTrader.snapTo(
				position.getX() + 0.5,
				position.getY(),
				position.getZ() + 0.5,
				random.nextFloat() * 360.0f,
				0.0f
		);
		if (!spawnedTrader.checkSpawnRules(level, EntitySpawnReason.EVENT)
				|| !spawnedTrader.checkSpawnObstruction(level)
				|| !level.noCollision(spawnedTrader)) {
			return false;
		}

		spawnedTrader.finalizeSpawn(
				level,
				level.getCurrentDifficultyAt(position),
				EntitySpawnReason.EVENT,
				null
		);
		return level.addFreshEntity(spawnedTrader);
	}

	/**
	 * Checks whether the player is underground and in sufficiently low light.
	 *
	 * @param player the player to inspect
	 * @return {@code true} if the player's position meets the dark-cave conditions
	 */
	private static boolean isInDarkCave(ServerPlayer player) {
		return isInDarkCave(player.level(), player.blockPosition());
	}

	private static boolean isInDarkCave(ServerLevel level, BlockPos position) {
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
		if (CompramosConfig.traderSpawningEnabled
				&& player.getRandom().nextInt(100) < CompramosConfig.traderSpawnChance) {
			spawnWanderingTrader(player.level(), player.blockPosition(), player.getRandom());
		}
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
