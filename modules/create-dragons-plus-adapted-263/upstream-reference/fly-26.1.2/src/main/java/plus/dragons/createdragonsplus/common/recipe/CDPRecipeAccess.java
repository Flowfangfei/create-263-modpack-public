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

import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Selects the live server recipe map or Fabric's synchronized client recipe view. */
public final class CDPRecipeAccess {
    private CDPRecipeAccess() {}

    public static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirst(
            Level level, RecipeType<T> type, I input) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipeFor(type, input, level);
        }
        return level.recipeAccess().getSynchronizedRecipes().getFirstMatch(type, input, level);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAll(
            Level level, RecipeType<T> type) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipes().stream()
                    .filter(holder -> holder.value().getType() == type)
                    .map(CDPRecipeAccess::<T>cast)
                    .toList();
        }
        return level.recipeAccess().getSynchronizedRecipes().getAllOfType(type);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> RecipeHolder<T> cast(RecipeHolder<?> holder) {
        return (RecipeHolder<T>) holder;
    }
}
