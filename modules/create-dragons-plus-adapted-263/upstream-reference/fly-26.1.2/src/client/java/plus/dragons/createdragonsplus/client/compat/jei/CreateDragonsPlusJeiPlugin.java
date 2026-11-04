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

package plus.dragons.createdragonsplus.client.compat.jei;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.Internal;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.client.compat.jei.category.AutomatedColoringCategory;
import plus.dragons.createdragonsplus.client.compat.jei.category.FanColoringCategory;
import plus.dragons.createdragonsplus.client.compat.jei.category.FanEndingCategory;
import plus.dragons.createdragonsplus.client.compat.jei.category.FanFreezingCategory;
import plus.dragons.createdragonsplus.client.compat.jei.category.FanSandingCategory;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingRecipe;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.registry.CDPRecipes;

@JeiPlugin
public final class CreateDragonsPlusJeiPlugin implements IModPlugin {
    private static final Identifier ID = CreateDragonsPlus.id("jei_plugin");
    public static final IRecipeType<RecipeHolder<MixingRecipe>> AUTOMATED_COLORING = recipeHolderType("automated_coloring");
    public static final IRecipeType<RecipeHolder<ColoringRecipe>> FAN_COLORING = recipeHolderType("fan_coloring");
    public static final IRecipeType<RecipeHolder<FreezingRecipe>> FAN_FREEZING = recipeHolderType("fan_freezing");
    public static final IRecipeType<RecipeHolder<SandingRecipe>> FAN_SANDING = recipeHolderType("fan_sanding");
    public static final IRecipeType<RecipeHolder<EndingRecipe>> FAN_ENDING = recipeHolderType("fan_ending");

    @Override
    public @NotNull Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (CDPConfig.recipes().enableBulkColoring.get()) {
            registration.addRecipeCategories(new FanColoringCategory());
        }
        if (CDPConfig.features().dyeFluids.get()) {
            registration.addRecipeCategories(new AutomatedColoringCategory());
        }
        if (CDPConfig.recipes().enableBulkFreezing.get()) {
            registration.addRecipeCategories(new FanFreezingCategory());
        }
        if (CDPConfig.recipes().enableBulkSanding.get()) {
            registration.addRecipeCategories(new FanSandingCategory());
        }
        if (CDPConfig.recipes().enableBulkEnding.get()) {
            registration.addRecipeCategories(new FanEndingCategory());
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var prepared = Internal.getClientSyncedRecipes();
        boolean bulkColoring = CDPConfig.recipes().enableBulkColoring.get();
        boolean automatedColoring = CDPConfig.features().dyeFluids.get();
        if (bulkColoring || automatedColoring) {
            var coloring = ColoringJeiRecipes.getAllRecipes();
            if (bulkColoring) {
                registration.addRecipes(FAN_COLORING, coloring);
            }
            if (automatedColoring) {
                registration.addRecipes(AUTOMATED_COLORING, ColoringJeiRecipes.getMixingRecipes(coloring));
            }
        }
        if (CDPConfig.recipes().enableBulkFreezing.get()) {
            registration.addRecipes(FAN_FREEZING, prepared.byType(CDPRecipes.FREEZING.getType()).stream().toList());
        }
        if (CDPConfig.recipes().enableBulkSanding.get()) {
            registration.addRecipes(FAN_SANDING, prepared.byType(CDPRecipes.SANDING.getType()).stream().toList());
        }
        if (CDPConfig.recipes().enableBulkEnding.get()) {
            registration.addRecipes(FAN_ENDING, prepared.byType(CDPRecipes.ENDING.getType()).stream().toList());
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (CDPConfig.recipes().enableBulkColoring.get()) {
            registration.addCraftingStation(FAN_COLORING, AllItems.ENCASED_FAN);
        }
        if (CDPConfig.features().dyeFluids.get()) {
            registration.addCraftingStation(AUTOMATED_COLORING, AllItems.MECHANICAL_MIXER, AllItems.BASIN);
        }
        if (CDPConfig.recipes().enableBulkFreezing.get()) {
            registration.addCraftingStation(FAN_FREEZING, AllItems.ENCASED_FAN);
        }
        if (CDPConfig.recipes().enableBulkSanding.get()) {
            registration.addCraftingStation(FAN_SANDING, AllItems.ENCASED_FAN);
        }
        if (CDPConfig.recipes().enableBulkEnding.get()) {
            registration.addCraftingStation(FAN_ENDING, AllItems.ENCASED_FAN);
        }
    }

    @SuppressWarnings("unchecked")
    private static <R> IRecipeType<R> recipeHolderType(String path) {
        return (IRecipeType<R>) IRecipeType.create(CreateDragonsPlus.id(path), RecipeHolder.class);
    }
}
