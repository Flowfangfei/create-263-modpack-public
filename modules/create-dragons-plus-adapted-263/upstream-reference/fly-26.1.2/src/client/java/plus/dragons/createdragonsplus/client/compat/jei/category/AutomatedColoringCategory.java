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
import com.zurrtum.create.client.compat.jei.category.MixingCategory;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createdragonsplus.client.compat.jei.CreateDragonsPlusJeiPlugin;

public final class AutomatedColoringCategory extends MixingCategory {
    private static final Component TITLE = Component.translatable("recipe.create_dragons_plus.automated_coloring");
    private static final IDrawable ICON = DyeFluidCategoryHelper.createIcon(AllItems.MECHANICAL_MIXER.getDefaultInstance());

    @Override
    public @NotNull IRecipeType<RecipeHolder<MixingRecipe>> getRecipeType() {
        return CreateDragonsPlusJeiPlugin.AUTOMATED_COLORING;
    }

    @Override
    public @NotNull Component getTitle() {
        return TITLE;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return ICON;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder, RecipeHolder<MixingRecipe> entry, IFocusGroup focuses) {
        super.setRecipe(builder, entry, focuses);
        DyeFluidCategoryHelper.findColor(entry.value())
                .ifPresent(color -> DyeFluidCategoryHelper.addDyeItemLookupAlias(builder, color));
    }
}
