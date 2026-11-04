package com.simibubi.create.content.fluids.potion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder;
import com.simibubi.create.foundation.recipe.BrewingReagentIngredient;
import com.simibubi.create.foundation.recipe.CreateRecipeClientCache;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class PotionMixingRecipes {

	public static final List<Item> SUPPORTED_CONTAINERS =
		List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

	private static final Map<Level, CachedRecipes> CACHE = new WeakHashMap<>();

	public static synchronized List<RecipeHolder<MixingRecipe>> createRecipes(Level level) {
		return cached(level).recipes();
	}

	public static synchronized Map<Item, List<MixingRecipe>> sortRecipesByItem(Level level) {
		return cached(level).byItem();
	}

	public static synchronized void clearCache() {
		CACHE.clear();
	}

	private static CachedRecipes cached(Level level) {
		return CACHE.computeIfAbsent(level, key -> {
			List<RecipeHolder<MixingRecipe>> recipes = createRecipesImpl(key);
			return new CachedRecipes(recipes, sortRecipesByItem(recipes));
		});
	}

	private static List<RecipeHolder<MixingRecipe>> createRecipesImpl(Level level) {
		List<RecipeHolder<MixingRecipe>> mixingRecipes = new ArrayList<>();
		Collection<RecipeHolder<?>> recipes = level.recipeAccess() instanceof RecipeManager manager
			? manager.getRecipes() : CreateRecipeClientCache.getRecipes();
		int recipeIndex = 0;

		for (RecipeHolder<?> holder : recipes) {
			if (!(holder.value() instanceof BrewingRecipe recipe))
				continue;
			ItemStack output = recipe.getOutput().create();
			if (!SUPPORTED_CONTAINERS.contains(output.getItem()))
				continue;
			for (Item container : SUPPORTED_CONTAINERS) {
				for (Reference<Potion> potion : level.registryAccess()
					.lookupOrThrow(Registries.POTION).listElements().toList()) {
					ItemStack input = new ItemStack(container);
					input.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
					if (!recipe.getInput().test(input))
						continue;
					FluidStack from = PotionFluidHandler.getFluidFromPotionItem(input);
					FluidStack to = PotionFluidHandler.getFluidFromPotionItem(output);
					from.setAmount(1000);
					to.setAmount(1000);
					mixingRecipes.add(createRecipe("potion_mixing_" + recipeIndex++,
						BrewingReagentIngredient.of(recipe.getReagent()), from, to));
				}
			}
		}
		return mixingRecipes;
	}

	private static RecipeHolder<MixingRecipe> createRecipe(String id, Ingredient ingredient, FluidStack fromFluid,
		FluidStack toFluid) {
		Identifier recipeId = Create.asResource(id);
		MixingRecipe recipe = new Builder<>(MixingRecipe::new, recipeId)
			.require(ingredient)
			.require(new SizedFluidIngredient(DataComponentFluidIngredient.of(false, fromFluid), fromFluid.getAmount()))
			.output(toFluid)
			.requiresHeat(HeatCondition.HEATED)
			.build();

		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe);
	}

	private static Map<Item, List<MixingRecipe>> sortRecipesByItem(List<RecipeHolder<MixingRecipe>> all) {
		Map<Item, List<MixingRecipe>> byItem = new HashMap<>();
		Set<Item> processedItems = new HashSet<>();
		for (RecipeHolder<MixingRecipe> recipe : all) {
			for (Ingredient ingredient : recipe.value()
				.getIngredients()) {
				ingredient.items()
					.forEach(holder -> {
						Item item = holder.value();
						if (processedItems.add(item))
							byItem.computeIfAbsent(item, $ -> new ArrayList<>())
								.add(recipe.value());
					});
			}
			processedItems.clear();
		}
		return byItem;
	}

	private record CachedRecipes(List<RecipeHolder<MixingRecipe>> recipes, Map<Item, List<MixingRecipe>> byItem) {}

}
