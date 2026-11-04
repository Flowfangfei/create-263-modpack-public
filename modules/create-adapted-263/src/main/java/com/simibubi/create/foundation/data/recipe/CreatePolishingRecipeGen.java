package com.simibubi.create.foundation.data.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.advancements.Advancement;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.PolishingRecipeGen;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;

/**
 * Create's own Data Generation for the singular default polishing recipe
 * @see PolishingRecipeGen
 */
@SuppressWarnings("unused")
public final class CreatePolishingRecipeGen extends PolishingRecipeGen {

	GeneratedRecipe

	ROSE_QUARTZ = create(AllItems.ROSE_QUARTZ::get, b -> b.output(AllItems.POLISHED_ROSE_QUARTZ.get()))

	;

	public CreatePolishingRecipeGen(PackOutput output, CompletableFuture<Provider> registries) {
		super(output, registries, Create.ID);
	}

	public CreatePolishingRecipeGen(BootstrapContext<Recipe<?>> registries, BootstrapContext<Advancement> output) {
		super(registries, output, Create.ID);
	}
}
