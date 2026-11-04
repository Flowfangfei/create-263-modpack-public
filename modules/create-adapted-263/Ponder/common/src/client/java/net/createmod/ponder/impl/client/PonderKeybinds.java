package net.createmod.ponder.impl.client;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.api.client.platform.ModClientHooksHelper;
import net.createmod.ponder.api.Ponder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.network.chat.Component;

public enum PonderKeybinds {
	PONDER("ponder", InputConstants.KEY_W);

	public final Category CATEGORY = new Category(Ponder.id("ponder"));

	private final KeyMapping mapping;

	PonderKeybinds(String description, int defaultKey) {
		this.mapping = new KeyMapping("key.ponder." + description, defaultKey, CATEGORY);
	}

	public static void register(Consumer<KeyMapping> registrationCallback) {
		for (PonderKeybinds key : values()) {
			registrationCallback.accept(key.mapping);
		}
	}

	public boolean isDown() {
		return !this.mapping.isUnbound() && ModClientHooksHelper.INSTANCE.isKeyPressed(this.mapping);
	}

	public Component message() {
		return this.mapping.getTranslatedKeyMessage();
	}

}
