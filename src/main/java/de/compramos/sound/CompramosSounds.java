package de.compramos.sound;

import de.compramos.CompramosMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class CompramosSounds {
	public static final SoundEvent FULL_COMPRAMOS = create("full_compramos");
	public static final SoundEvent SIMPLE_COMPRAMOS = create("simple_compramos");

	private CompramosSounds() {
	}

	public static void register() {
	}

	private static SoundEvent create(String path) {
		Identifier id = Identifier.fromNamespaceAndPath(CompramosMod.MOD_ID, path);
		return Registry.register(
				BuiltInRegistries.SOUND_EVENT,
				id,
				SoundEvent.createVariableRangeEvent(id)
		);
	}
}
