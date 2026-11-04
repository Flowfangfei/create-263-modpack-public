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

package plus.dragons.createdragonsplus.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createdragonsplus.client.compat.jei.CreateDragonsPlusJeiPlugin;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.fluid.FluidUnits;
import plus.dragons.createdragonsplus.registry.CDPFluids;

public final class FanColoringCategory extends AbstractFanCategory<ColoringRecipe> {
    public FanColoringCategory() {
        super("coloring", DyeFluidCategoryHelper.createIcon(AllItems.PROPELLER.getDefaultInstance()));
    }

    @Override
    public @NotNull IRecipeType<RecipeHolder<ColoringRecipe>> getRecipeType() {
        return CreateDragonsPlusJeiPlugin.FAN_COLORING;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder, RecipeHolder<ColoringRecipe> entry, IFocusGroup focuses) {
        super.setRecipe(builder, entry, focuses);
        var fluid = CDPFluids.DYES_BY_VARIANT.get(entry.value().color());
        if (fluid == null) {
            return;
        }
        builder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
                .add(fluid.getSource(), FluidUnits.BUCKET, DataComponentPatch.EMPTY)
                .add(fluid.getBucket().getDefaultInstance());
        DyeFluidCategoryHelper.addDyeItemLookupAlias(builder, entry.value().color());
    }

    @Override
    protected Ingredient ingredient(ColoringRecipe recipe) {
        return recipe.ingredient();
    }

    @Override
    protected List<ProcessingOutput> results(ColoringRecipe recipe) {
        return recipe.results();
    }

    @Override
    protected BlockState catalystState(RecipeHolder<ColoringRecipe> entry) {
        var fluid = CDPFluids.DYES_BY_VARIANT.get(entry.value().color());
        return fluid == null ? Blocks.WATER.defaultBlockState() : fluid.getBlock().defaultBlockState();
    }
}
