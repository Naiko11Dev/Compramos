package de.compramos.client;

import de.compramos.CompramosMod;
import de.compramos.sound.CompramosSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class CompramosClient implements ClientModInitializer {
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

	private static int playSound(FabricClientCommandSource source, SoundEvent sound, String name) {
		source.getPlayer().playSound(sound, 1.0f, 1.0f);
		source.sendFeedback(Component.literal("Playing the " + name + " Compramos sound."));
		return 1;
	}
}
