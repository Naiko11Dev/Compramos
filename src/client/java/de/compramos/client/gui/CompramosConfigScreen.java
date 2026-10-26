package de.compramos.client.gui;

import de.compramos.CompramosConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class CompramosConfigScreen extends Screen {
	private static final List<Integer> FREQUENCIES = List.of(0, 1, 2);
	private final Screen parent;

	public CompramosConfigScreen(Screen parent) {
		super(Component.literal("Compramos Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = width / 2 - 150;
		int y = height / 2 - 114;
		int rowHeight = 26;

		addRenderableWidget(CycleButton.onOffBuilder(CompramosConfig.traderSoundsEnabled)
				.create(x, y, 300, 20, Component.literal("Wandering Trader sounds"), (button, enabled) -> {
					CompramosConfig.traderSoundsEnabled = enabled;
					CompramosConfig.save();
				}));
		addRenderableWidget(CycleButton.builder(
						CompramosConfigScreen::traderFrequencyLabel,
						CompramosConfig.traderSoundFrequency
				).withValues(FREQUENCIES)
				.create(x, y + rowHeight, 300, 20, Component.literal("Trader sound frequency"), (button, value) -> {
					CompramosConfig.traderSoundFrequency = value;
					CompramosConfig.save();
				}));
		addRenderableWidget(CycleButton.onOffBuilder(CompramosConfig.montoyaOnTraderDespawnEnabled)
				.create(x, y + rowHeight * 2, 300, 20, Component.literal("Montoya on trader despawn"), (button, enabled) -> {
					CompramosConfig.montoyaOnTraderDespawnEnabled = enabled;
					CompramosConfig.save();
				}));
		addRenderableWidget(CycleButton.onOffBuilder(CompramosConfig.terreneitorOnDonkeyMountEnabled)
				.create(x, y + rowHeight * 3, 300, 20, Component.literal("Terreneitor when mounting a donkey"), (button, enabled) -> {
					CompramosConfig.terreneitorOnDonkeyMountEnabled = enabled;
					CompramosConfig.save();
				}));
		addRenderableWidget(CycleButton.onOffBuilder(CompramosConfig.caveSoundsEnabled)
				.create(x, y + rowHeight * 4, 300, 20, Component.literal("Cave ambience"), (button, enabled) -> {
					CompramosConfig.caveSoundsEnabled = enabled;
					CompramosConfig.save();
				}));
		addRenderableWidget(CycleButton.builder(
						CompramosConfigScreen::caveFrequencyLabel,
						CompramosConfig.caveSoundFrequency
				).withValues(FREQUENCIES)
				.create(x, y + rowHeight * 5, 300, 20, Component.literal("Cave sound frequency"), (button, value) -> {
					CompramosConfig.caveSoundFrequency = value;
					CompramosConfig.save();
				}));
		addRenderableWidget(CycleButton.onOffBuilder(CompramosConfig.caveParticlesEnabled)
				.create(x, y + rowHeight * 6, 300, 20, Component.literal("Cave particles"), (button, enabled) -> {
					CompramosConfig.caveParticlesEnabled = enabled;
					CompramosConfig.save();
				}));
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds(x, y + rowHeight * 7, 300, 20)
				.build());
	}

	@Override
	public void onClose() {
		minecraft.setScreenAndShow(parent);
	}

	private static Component traderFrequencyLabel(int frequency) {
		return Component.literal(switch (frequency) {
			case 1 -> "5-10 minutes";
			case 2 -> "10-20 minutes";
			default -> "1-2 minutes";
		});
	}

	private static Component caveFrequencyLabel(int frequency) {
		return Component.literal(switch (frequency) {
			case 0 -> "30-60 minutes";
			case 2 -> "2-3 hours";
			default -> "1.5-2.5 hours";
		});
	}
}
