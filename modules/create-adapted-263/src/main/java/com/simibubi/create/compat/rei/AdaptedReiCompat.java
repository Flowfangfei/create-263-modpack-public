package com.simibubi.create.compat.rei;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.common.NeoForge;

public final class AdaptedReiCompat {
	private static final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> RECIPES = new LinkedHashMap<>();
	private static boolean dirty;
	private static int cooldown;

	public AdaptedReiCompat() {
		NeoForge.EVENT_BUS.addListener(this::onRecipes);
		NeoForge.EVENT_BUS.addListener(this::tick);
		NeoForge.EVENT_BUS.addListener(this::logout);
	}

	private void onRecipes(RecipesReceivedEvent event) {
		synchronized (RECIPES) {
			RECIPES.values().removeIf(recipe -> event.getRecipeTypes().contains(recipe.value().getType()));
			event.getRecipeMap().values().forEach(recipe -> RECIPES.put(recipe.id(), recipe));
		}
		dirty = true;
		cooldown = 10;
	}

	private void tick(Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (dirty && minecraft.level != null && minecraft.getConnection() != null && --cooldown <= 0) {
			dirty = false;
			RoughlyEnoughItemsCoreClient.reloadPlugins(null, null);
		}
	}

	private void logout(LoggingOut event) {
		synchronized (RECIPES) {
			RECIPES.clear();
		}
		dirty = false;
		cooldown = 0;
	}

	public static List<RecipeHolder<?>> recipes() {
		synchronized (RECIPES) {
			return List.copyOf(RECIPES.values());
		}
	}
}
