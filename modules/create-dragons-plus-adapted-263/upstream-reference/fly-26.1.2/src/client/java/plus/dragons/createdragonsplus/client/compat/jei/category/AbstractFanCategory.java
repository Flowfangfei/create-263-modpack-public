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
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;

abstract class AbstractFanCategory<R extends Recipe<?>> extends CreateCategory<RecipeHolder<R>> {
    private final Component title;
    private final IDrawable icon;

    protected AbstractFanCategory(String name, ItemStack catalystIcon) {
        this(name, new TwoIconRenderer(AllItems.PROPELLER.getDefaultInstance(), catalystIcon));
    }

    protected AbstractFanCategory(String name, IDrawable icon) {
        this.title = Component.translatable("recipe.create_dragons_plus.fan_" + name);
        this.icon = icon;
    }

    @Override
    public final Component getTitle() {
        return title;
    }

    @Override
    public final IDrawable getIcon() {
        return icon;
    }

    @Override
    public final int getHeight() {
        return 72;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<R> entry, IFocusGroup focuses) {
        R recipe = entry.value();
        List<ProcessingOutput> results = results(recipe);
        int outputSize = results.size();
        if (outputSize == 1) {
            builder.addInputSlot(21, 48).setBackground(SLOT, -1, -1).add(ingredient(recipe));
            addChanceSlot(builder, 141, 48, results.getFirst());
            return;
        }
        int xOffsetAmount = 1 - Math.min(3, outputSize);
        builder.addInputSlot(21 + xOffsetAmount * 5, 48)
                .setBackground(SLOT, -1, -1)
                .add(ingredient(recipe));
        for (int index = 0, left = 141 + xOffsetAmount * 9, top = outputSize <= 9 ? 48 : 57; index < outputSize; index++) {
            addChanceSlot(builder, left + (index % 3) * 19, top + (index / 3) * -19, results.get(index));
        }
    }

    @Override
    public void draw(
            RecipeHolder<R> entry,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor graphics,
            double mouseX,
            double mouseY) {
        int xOffsetAmount = 1 - Math.min(3, results(entry.value()).size());
        AllGuiTextures.JEI_SHADOW.render(graphics, 46, 27);
        AllGuiTextures.JEI_SHADOW.render(graphics, 65, 39);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54 + 7 * xOffsetAmount, 51);
        graphics.guiRenderState.addPicturesInPictureState(new FanRenderState(
                new Matrix3x2f(graphics.pose()), 56, 4, catalystState(entry)));
    }

    protected abstract Ingredient ingredient(R recipe);

    protected abstract List<ProcessingOutput> results(R recipe);

    protected abstract BlockState catalystState(RecipeHolder<R> entry);
}
