package com.simibubi.create.foundation.data.recipe;

import java.util.Set;
import com.simibubi.create.Create;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Registers Create recipes through the Minecraft 26.3 reloadable-registry bootstrap API. */
public final class RecipeData26 {
	public static void gatherData(GatherDataEvent.Server event) {
		RegistrySetBuilder recipes = new RegistrySetBuilder();
		recipes.add(RecipeProvider.asBootstrap(CreateCompactingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateCrushingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateCuttingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateDeployingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateEmptyingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateFillingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateHauntingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateItemApplicationRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateMillingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateMixingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateMechanicalCraftingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreatePolishingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreatePressingRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateSequencedAssemblyRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateStandardRecipeGen::new));
		recipes.add(RecipeProvider.asBootstrap(CreateWashingRecipeGen::new));
		event.createReloadableRegistryObjects(recipes, Set.of(Create.ID), "Create recipes");
	}
}
