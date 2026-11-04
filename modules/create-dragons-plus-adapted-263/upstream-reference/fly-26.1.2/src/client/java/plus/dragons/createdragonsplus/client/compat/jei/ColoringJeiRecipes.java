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

import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import mezz.jei.common.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.registry.CDPFluids;
import plus.dragons.createdragonsplus.registry.CDPRecipes;
import plus.dragons.createdragonsplus.registry.CDPTags;

public final class ColoringJeiRecipes {
    private ColoringJeiRecipes() {}

    public static List<RecipeHolder<ColoringRecipe>> getAllRecipes() {
        var prepared = Internal.getClientSyncedRecipes();
        List<RecipeHolder<ColoringRecipe>> recipes = new ArrayList<>(prepared.byType(CDPRecipes.COLORING.getType()));
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return recipes;
        }
        for (RecipeHolder<CraftingRecipe> holder : prepared.byType(RecipeType.CRAFTING)) {
            CraftingRecipe crafting = holder.value();
            if (crafting.isSpecial()) {
                continue;
            }
            for (DyeVariant variant : DyeVariantRegistry.all()) {
                convert(holder, crafting, variant).ifPresent(recipes::add);
            }
        }
        recipes.removeIf(holder -> !CDPFluids.DYES_BY_VARIANT.containsKey(holder.value().color()));
        recipes.sort(Comparator
                .<RecipeHolder<ColoringRecipe>>comparingInt(
                        holder -> DyeVariantRegistry.creativeModeTabIndex(holder.value().color()))
                .thenComparing(holder -> holder.id().identifier().toString()));
        return recipes;
    }

    public static List<RecipeHolder<MixingRecipe>> getMixingRecipes(
            List<RecipeHolder<ColoringRecipe>> coloringRecipes) {
        List<RecipeHolder<MixingRecipe>> recipes = new ArrayList<>(coloringRecipes.size());
        for (RecipeHolder<ColoringRecipe> holder : coloringRecipes) {
            ColoringRecipe coloring = holder.value();
            var fluid = CDPFluids.DYES_BY_VARIANT.get(coloring.color());
            if (fluid == null || coloring.results().isEmpty() || coloring.results().size() > 4) {
                continue;
            }
            MixingRecipe mixing = new MixingRecipe(
                    100,
                    coloring.results(),
                    List.of(),
                    HeatCondition.NONE,
                    List.of(new FluidStackIngredient(
                            fluid.getSource(), DataComponentPatch.EMPTY, coloring.dyeFluidAmount())),
                    List.of(new SizedIngredient(coloring.ingredient(), 1)));
            Identifier source = holder.id().identifier();
            Identifier id = CreateDragonsPlus.id(
                    "jei/mixing/" + source.getNamespace() + "/" + source.getPath());
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
            recipes.add(new RecipeHolder<>(key, mixing));
        }
        return recipes;
    }

    private static Optional<RecipeHolder<ColoringRecipe>> convert(
            RecipeHolder<CraftingRecipe> holder, CraftingRecipe recipe, DyeVariant variant) {
        ItemStack dye = variant.dyeItemStack();
        if (dye.isEmpty()) {
            return Optional.empty();
        }
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        Conversion conversion;
        if (ingredients.size() == 2) {
            conversion = convertTwoIngredients(recipe, ingredients, dye);
        } else if (ingredients.size() == 9) {
            conversion = convertNineIngredients(recipe, ingredients, dye);
        } else {
            return Optional.empty();
        }
        if (conversion == null || conversion.result().is(dye.getItem())) {
            return Optional.empty();
        }
        Ingredient input = withoutExcludedItems(conversion.input());
        if (input.isEmpty()) {
            return Optional.empty();
        }
        ColoringRecipe coloring = new ColoringRecipe(
                List.of(new ProcessingOutput(conversion.result().copyWithCount(1))),
                List.of(input),
                variant.id(),
                ColoringRecipe.DEFAULT_DYE_FLUID_AMOUNT);
        Identifier source = holder.id().identifier();
        Identifier id = CreateDragonsPlus.id("jei/coloring/"
                + variant.serializedName()
                + "/"
                + source.getNamespace()
                + "/"
                + source.getPath());
        return Optional.of(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), coloring));
    }

    private static Conversion convertTwoIngredients(
            CraftingRecipe recipe, List<Ingredient> ingredients, ItemStack dye) {
        int dyeIndex = -1;
        for (int index = 0; index < ingredients.size(); index++) {
            if (ingredients.get(index).test(dye)) {
                if (dyeIndex != -1) {
                    return null;
                }
                dyeIndex = index;
            }
        }
        if (dyeIndex == -1) {
            return null;
        }
        Ingredient input = ingredients.get(1 - dyeIndex);
        Optional<ItemStack> representative = firstStack(input);
        if (representative.isEmpty()) {
            return null;
        }
        List<ItemStack> stacks = dyeIndex == 0
                ? List.of(dye.copyWithCount(1), representative.get())
                : List.of(representative.get(), dye.copyWithCount(1));
        return assemble(recipe, CraftingInput.of(2, 1, stacks), input, 1)
                .or(() -> assemble(recipe, CraftingInput.of(1, 2, stacks), input, 1))
                .orElse(null);
    }

    private static Conversion convertNineIngredients(
            CraftingRecipe recipe, List<Ingredient> ingredients, ItemStack dye) {
        int dyeIndex = -1;
        Ingredient input = null;
        ItemStack representative = ItemStack.EMPTY;
        for (int index = 0; index < ingredients.size(); index++) {
            Ingredient ingredient = ingredients.get(index);
            if (ingredient.test(dye)) {
                if (dyeIndex != -1) {
                    return null;
                }
                dyeIndex = index;
                continue;
            }
            if (input == null) {
                input = ingredient;
                representative = firstStack(ingredient).orElse(ItemStack.EMPTY);
                if (representative.isEmpty()) {
                    return null;
                }
            } else if (!ingredient.test(representative)) {
                return null;
            }
        }
        if (dyeIndex == -1 || input == null) {
            return null;
        }
        List<ItemStack> stacks = new ArrayList<>(9);
        for (int index = 0; index < 9; index++) {
            stacks.add(index == dyeIndex ? dye.copyWithCount(1) : representative.copyWithCount(1));
        }
        return assemble(recipe, CraftingInput.of(3, 3, stacks), input, 8).orElse(null);
    }

    private static Optional<Conversion> assemble(
            CraftingRecipe recipe, CraftingInput craftingInput, Ingredient input, int expectedCount) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return Optional.empty();
        }
        try {
            if (!recipe.matches(craftingInput, level)) {
                return Optional.empty();
            }
            ItemStack result = recipe.assemble(craftingInput);
            return !result.isEmpty() && result.getCount() == expectedCount
                    ? Optional.of(new Conversion(input, result))
                    : Optional.empty();
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static Ingredient withoutExcludedItems(Ingredient ingredient) {
        return Ingredient.of(ingredient.items()
                .map(holder -> holder.value())
                .filter(item -> !item.getDefaultInstance().is(CDPTags.NOT_APPLICABLE_COLORING)));
    }

    private static Optional<ItemStack> firstStack(Ingredient ingredient) {
        return ingredient.items().findFirst().map(ItemStack::new);
    }

    private record Conversion(Ingredient input, ItemStack result) {}
}
