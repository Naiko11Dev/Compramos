/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Naiko11
 */

package de.compramos.client;

import de.compramos.CompramosConfig;
import de.compramos.CompramosMod;
import de.compramos.sound.CompramosSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * Registers client-side commands and initializes client-specific mod behavior.
 */
public final class CompramosClient implements ClientModInitializer {
	/**
	 * Registers the {@code /compramos test} sound playback commands.
	 */
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(literal("compramos")
						.then(literal("test")
								.then(literal("full").executes(context ->
										playSound(context.getSource(), CompramosSounds.FULL_COMPRAMOS, "full")))
								.then(literal("simple").executes(context ->
										playSound(context.getSource(), CompramosSounds.SIMPLE_COMPRAMOS, "simple")))
								.then(literal("montoya").executes(context ->
										playSound(context.getSource(), CompramosSounds.MONTOYA_POR_FAVOR, "Montoya")))
								.then(literal("terreneitor").executes(context ->
										playSound(context.getSource(), CompramosSounds.TERRENEITOR, "Terreneitor"))))));

		CompramosMod.LOGGER.info("Initializing Compramos client.");
	}

	/**
	 * Plays one of the mod's sounds for the command's player.
	 *
	 * @param source the player and feedback context for the command
	 * @param sound the sound event to play
	 * @param name the readable sound name shown in command feedback
	 * @return the command success result
	 */
	private static int playSound(FabricClientCommandSource source, SoundEvent sound, String name) {
		source.getPlayer().playSound(sound, CompramosConfig.soundVolume, 1.0f);
		source.sendFeedback(Component.literal("Playing the " + name + " Compramos sound."));
		return 1;
	}
}
