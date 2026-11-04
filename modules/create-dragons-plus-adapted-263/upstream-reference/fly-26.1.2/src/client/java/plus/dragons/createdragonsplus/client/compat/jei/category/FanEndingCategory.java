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

import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import java.util.List;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createdragonsplus.client.compat.jei.CreateDragonsPlusJeiPlugin;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingRecipe;
import plus.dragons.createdragonsplus.registry.CDPFluids;

public final class FanEndingCategory extends AbstractFanCategory<EndingRecipe> {
    public FanEndingCategory() {
        super("ending", Items.DRAGON_BREATH.getDefaultInstance());
    }

    @Override
    public @NotNull IRecipeType<RecipeHolder<EndingRecipe>> getRecipeType() {
        return CreateDragonsPlusJeiPlugin.FAN_ENDING;
    }

    @Override
    protected Ingredient ingredient(EndingRecipe recipe) {
        return recipe.ingredient();
    }

    @Override
    protected List<ProcessingOutput> results(EndingRecipe recipe) {
        return recipe.results();
    }

    @Override
    protected BlockState catalystState(RecipeHolder<EndingRecipe> entry) {
        return CDPFluids.DRAGON_BREATH.getBlock().defaultBlockState();
    }
}
