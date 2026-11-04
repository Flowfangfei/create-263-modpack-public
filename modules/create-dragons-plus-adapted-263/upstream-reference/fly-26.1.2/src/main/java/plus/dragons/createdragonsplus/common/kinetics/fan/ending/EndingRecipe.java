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

package plus.dragons.createdragonsplus.common.kinetics.fan.ending;

import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import java.util.List;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import plus.dragons.createdragonsplus.common.kinetics.fan.SimpleFanRecipeSerializer;
import plus.dragons.createdragonsplus.registry.CDPRecipes;

public record EndingRecipe(List<ProcessingOutput> results, List<Ingredient> ingredients)
        implements CreateSingleStackRollableRecipe {
    public EndingRecipe {
        results = List.copyOf(results);
        ingredients = List.copyOf(ingredients);
        if (ingredients.size() != 1) {
            throw new IllegalArgumentException("Ending recipes require exactly one ingredient");
        }
    }

    @Override
    public Ingredient ingredient() {
        return ingredients.getFirst();
    }

    @Override
    public RecipeSerializer<EndingRecipe> getSerializer() {
        return CDPRecipes.ENDING.getSerializer();
    }

    @Override
    public RecipeType<EndingRecipe> getType() {
        return CDPRecipes.ENDING.getType();
    }

    public static final class Serializer {
        public static final RecipeSerializer<EndingRecipe> INSTANCE = SimpleFanRecipeSerializer.create(EndingRecipe::new);

        private Serializer() {}
    }
}
