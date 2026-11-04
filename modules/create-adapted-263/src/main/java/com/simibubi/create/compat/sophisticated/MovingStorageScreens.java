package com.simibubi.create.compat.sophisticated;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsScreen;

public final class MovingStorageScreens {
	@SubscribeEvent
	public static void register(RegisterMenuScreensEvent event) {
		event.register(SophisticatedContraptionCompat.MENU.get(), InventoryScreen::new);
		MenuType<MovingStorageSettingsMenu> settings = SophisticatedContraptionCompat.SETTINGS.get();
		event.register(settings, SettingsScreen::constructScreen);
	}

	public static final class InventoryScreen extends StorageScreenBase<MovingStorageMenu> {
		public InventoryScreen(MovingStorageMenu menu, Inventory inventory, Component title) {
			super(menu, inventory, title);
		}

		@Override
		protected String getStorageSettingsTabTooltip() {
			return "gui.sophisticatedstorage.settings.tooltip";
		}
	}

	public static final class SettingsScreen extends StorageSettingsScreen {
		public static SettingsScreen constructScreen(SettingsContainerMenu<?> menu, Inventory inventory, Component title) {
			return new SettingsScreen((MovingStorageSettingsMenu) menu, inventory, title);
		}

		public SettingsScreen(MovingStorageSettingsMenu menu, Inventory inventory, Component title) {
			super(menu, inventory, title);
		}

		@Override
		protected void sendStorageInventoryScreenOpenMessage() {
			((MovingStorageSettingsMenu) menu).openInventory();
		}
	}
}
