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

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import java.util.Map;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.registry.CDPFluids;

final class DyeFluidCategoryHelper {
    private static ItemStack[] dyeBucketIcons;

    private DyeFluidCategoryHelper() {}

    static Optional<Identifier> findColor(MixingRecipe recipe) {
        if (recipe.fluidIngredients().size() != 1) {
            return Optional.empty();
        }
        var matchingFluids = recipe.fluidIngredients().getFirst().getMatchingFluids();
        return CDPFluids.DYES_BY_VARIANT.entrySet().stream()
                .filter(entry -> matchingFluids.contains(entry.getValue().getSource()))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    static void addDyeItemLookupAlias(IRecipeLayoutBuilder builder, Identifier color) {
        DyeVariantRegistry.get(color).ifPresent(variant -> BuiltInRegistries.ITEM
                .get(variant.dyeItemTag())
                .map(Ingredient::of)
                .filter(dyeItems -> !dyeItems.isEmpty())
                .ifPresent(dyeItems -> builder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
                        .add(dyeItems)));
    }

    static IDrawable createIcon(ItemStack primary) {
        return new CyclingDyeIcon(primary);
    }

    private static ItemStack getDyeBucketIcon() {
        if (dyeBucketIcons == null) {
            dyeBucketIcons = DyeVariantRegistry.all().stream()
                    .map(variant -> CDPFluids.DYES_BY_VARIANT.get(variant.id()))
                    .map(fluid -> fluid.getBucket().getDefaultInstance())
                    .toArray(ItemStack[]::new);
        }
        if (dyeBucketIcons.length == 0) {
            return ItemStack.EMPTY;
        }
        return dyeBucketIcons[(AnimationTickHolder.getTicks() / 20) % dyeBucketIcons.length];
    }

    private record CyclingDyeIcon(ItemStack primary) implements IDrawable {
        @Override
        public int getWidth() {
            return 18;
        }

        @Override
        public int getHeight() {
            return 18;
        }

        @Override
        public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
            new TwoIconRenderer(primary, getDyeBucketIcon()).draw(graphics, xOffset, yOffset);
        }
    }
}
