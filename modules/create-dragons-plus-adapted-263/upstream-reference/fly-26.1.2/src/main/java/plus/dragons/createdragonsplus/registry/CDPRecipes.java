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

package plus.dragons.createdragonsplus.registry;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.recipe.CDPRecipeTypeInfo;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingRecipe;

public final class CDPRecipes {
    private static RecipeType<ColoringRecipe> coloringType;
    private static RecipeType<FreezingRecipe> freezingType;
    private static RecipeType<SandingRecipe> sandingType;
    private static RecipeType<EndingRecipe> endingType;
    private static RecipeSerializer<ColoringRecipe> coloringSerializer;
    private static RecipeSerializer<FreezingRecipe> freezingSerializer;
    private static RecipeSerializer<SandingRecipe> sandingSerializer;
    private static RecipeSerializer<EndingRecipe> endingSerializer;
    public static final CDPRecipeTypeInfo<ColoringRecipe> COLORING = info(
            "coloring", () -> coloringType, () -> coloringSerializer);
    public static final CDPRecipeTypeInfo<FreezingRecipe> FREEZING = info(
            "freezing", () -> freezingType, () -> freezingSerializer);
    public static final CDPRecipeTypeInfo<SandingRecipe> SANDING = info(
            "sanding", () -> sandingType, () -> sandingSerializer);
    public static final CDPRecipeTypeInfo<EndingRecipe> ENDING = info("ending", () -> endingType, () -> endingSerializer);
    private static boolean registered;

    private CDPRecipes() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        coloringType = registerType(COLORING.getId());
        freezingType = registerType(FREEZING.getId());
        sandingType = registerType(SANDING.getId());
        endingType = registerType(ENDING.getId());
        coloringSerializer = registerSerializer(COLORING.getId(), ColoringRecipe.Serializer.INSTANCE);
        freezingSerializer = registerSerializer(FREEZING.getId(), FreezingRecipe.Serializer.INSTANCE);
        sandingSerializer = registerSerializer(SANDING.getId(), SandingRecipe.Serializer.INSTANCE);
        endingSerializer = registerSerializer(ENDING.getId(), EndingRecipe.Serializer.INSTANCE);
        RecipeSynchronization.synchronizeRecipeSerializer(coloringSerializer);
        RecipeSynchronization.synchronizeRecipeSerializer(freezingSerializer);
        RecipeSynchronization.synchronizeRecipeSerializer(sandingSerializer);
        RecipeSynchronization.synchronizeRecipeSerializer(endingSerializer);
    }

    private static <R extends Recipe<?>> CDPRecipeTypeInfo<R> info(
            String name,
            Supplier<? extends RecipeType<R>> type,
            Supplier<? extends RecipeSerializer<R>> serializer) {
        return new CDPRecipeTypeInfo<>(CreateDragonsPlus.id(name), type, serializer);
    }

    private static <R extends Recipe<?>> RecipeType<R> registerType(Identifier id) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    private static <R extends Recipe<?>> RecipeSerializer<R> registerSerializer(
            Identifier id, RecipeSerializer<R> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer);
    }
}
