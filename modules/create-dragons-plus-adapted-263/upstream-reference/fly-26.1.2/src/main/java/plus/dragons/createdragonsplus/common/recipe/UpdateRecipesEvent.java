/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.common.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.mixin.minecraft.RecipeManagerAccessor;

/** Fired on the logical server after static registry tags have been rebound for a data-pack reload. */
public final class UpdateRecipesEvent {
    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, listeners -> event -> {
        for (Listener listener : listeners) {
            listener.updateRecipes(event);
        }
    });

    private final RecipeManager recipeManager;
    private final LinkedHashMap<ResourceKey<Recipe<?>>, RecipeHolder<?>> recipes;
    private int added;
    private int removed;

    private UpdateRecipesEvent(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
        this.recipes = new LinkedHashMap<>();
        recipeManager.getRecipes().forEach(recipe -> recipes.put(recipe.id(), recipe));
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public Optional<RecipeHolder<?>> getRecipe(ResourceKey<Recipe<?>> id) {
        return Optional.ofNullable(recipes.get(id));
    }

    public Optional<RecipeHolder<?>> getRecipe(Identifier id) {
        return getRecipe(ResourceKey.create(Registries.RECIPE, id));
    }

    public void addRecipe(RecipeHolder<?> recipe) {
        if (recipes.put(recipe.id(), recipe) == null) {
            added++;
        }
    }

    public void removeRecipe(ResourceKey<Recipe<?>> id) {
        if (recipes.remove(id) != null) {
            removed++;
        }
    }

    public void removeRecipe(RecipeHolder<?> recipe) {
        removeRecipe(recipe.id());
    }

    public Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> recipes() {
        return Map.copyOf(recipes);
    }

    public static void fire(RecipeManager recipeManager) {
        UpdateRecipesEvent event = new UpdateRecipesEvent(recipeManager);
        EVENT.invoker().updateRecipes(event);
        ((RecipeManagerAccessor) recipeManager).createDragonsPlus$setRecipes(RecipeMap.create(event.recipes.values()));
        CreateDragonsPlus.LOGGER.debug(
                "Updated RecipeMap with {} additions and {} removals", event.added, event.removed);
    }

    @FunctionalInterface
    public interface Listener {
        void updateRecipes(UpdateRecipesEvent event);
    }
}
