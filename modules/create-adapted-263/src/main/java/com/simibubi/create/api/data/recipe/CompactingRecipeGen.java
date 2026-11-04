package com.simibubi.create.api.data.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.advancements.Advancement;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllRecipeTypes;

import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

/**
 * The base class for Compacting recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateCompactingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class CompactingRecipeGen extends StandardProcessingRecipeGen<CompactingRecipe> {

	public CompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	protected CompactingRecipeGen(BootstrapContext<Recipe<?>> registries, BootstrapContext<Advancement> output, String defaultNamespace) {
		super(registries, output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.COMPACTING;
	}

}
