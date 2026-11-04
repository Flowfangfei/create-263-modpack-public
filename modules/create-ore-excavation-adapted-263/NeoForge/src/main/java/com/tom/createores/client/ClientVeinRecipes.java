package com.tom.createores.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import com.tom.createores.CreateOreExcavation;
import com.tom.createores.recipe.VeinRecipe;

@EventBusSubscriber(value = Dist.CLIENT, modid = CreateOreExcavation.MODID)
public final class ClientVeinRecipes {
    private static Map<ResourceKey<Recipe<?>>, RecipeHolder<VeinRecipe>> recipes = Map.of();
    private static Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> allRecipes = Map.of();

    private ClientVeinRecipes() {}

    @SubscribeEvent
    public static void received(RecipesReceivedEvent event) {
        Map<ResourceKey<Recipe<?>>, RecipeHolder<VeinRecipe>> next = new HashMap<>();

        for (RecipeHolder<VeinRecipe> holder :
                event.getRecipeMap().byType(CreateOreExcavation.VEIN_RECIPES.getRecipeType())) {
            next.put(holder.id(), holder);
        }

        recipes = Map.copyOf(next);
        Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> all = new HashMap<>(next);
        event.getRecipeMap().byType(CreateOreExcavation.DRILLING_RECIPES.getRecipeType()).forEach(r -> all.put(r.id(), r));
        event.getRecipeMap().byType(CreateOreExcavation.EXTRACTING_RECIPES.getRecipeType()).forEach(r -> all.put(r.id(), r));
        allRecipes = Map.copyOf(all);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = Map.of();
        allRecipes = Map.of();
    }

    public static Optional<RecipeHolder<?>> recipeById(Identifier id) {
        return Optional.ofNullable(allRecipes.get(CreateOreExcavation.recipeKey(id)));
    }

    public static Optional<RecipeHolder<VeinRecipe>> byId(Identifier id) {
        return Optional.ofNullable(recipes.get(CreateOreExcavation.recipeKey(id)));
    }
}
