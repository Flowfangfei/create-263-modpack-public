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

package plus.dragons.createdragonsplus.common.kinetics.fan.coloring;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.foundation.recipe.CreateRollableRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.fluid.FluidUnits;
import plus.dragons.createdragonsplus.registry.CDPRecipes;

public record ColoringRecipe(
        List<ProcessingOutput> results,
        List<Ingredient> ingredients,
        Identifier color,
        int dyeFluidAmount)
        implements CreateRollableRecipe<ColoringRecipeInput> {
    public static final int DEFAULT_DYE_FLUID_AMOUNT = (int) FluidUnits.AUTOMATIC_COLORING;
    public static final int MAX_DYE_FLUID_AMOUNT = (int) FluidUnits.BUCKET;

    public ColoringRecipe {
        results = List.copyOf(results);
        ingredients = List.copyOf(ingredients);
        if (ingredients.size() != 1) {
            throw new IllegalArgumentException("Coloring recipes require exactly one ingredient");
        }
        if (dyeFluidAmount <= 0 || dyeFluidAmount > MAX_DYE_FLUID_AMOUNT) {
            throw new IllegalArgumentException("Dye fluid amount must be in [1, " + MAX_DYE_FLUID_AMOUNT + "]");
        }
    }

    public Ingredient ingredient() {
        return ingredients.getFirst();
    }

    @Override
    public boolean matches(ColoringRecipeInput input, Level level) {
        return color.equals(input.color()) && ingredient().test(input.item());
    }

    @Override
    public List<ItemStack> assemble(ColoringRecipeInput input, RandomSource random) {
        ItemStack junk = CreateRecipe.getJunk(input.item());
        if (junk != null) {
            return List.of(junk);
        }
        List<ItemStack> outputs = new ArrayList<>(results.size());
        ProcessingOutput.rollOutput(random, results, outputs::add);
        return outputs;
    }

    @Override
    public RecipeSerializer<ColoringRecipe> getSerializer() {
        return CDPRecipes.COLORING.getSerializer();
    }

    @Override
    public RecipeType<ColoringRecipe> getType() {
        return CDPRecipes.COLORING.getType();
    }

    public static final class Serializer {
        private static final StreamCodec<RegistryFriendlyByteBuf, List<ProcessingOutput>> OUTPUTS_STREAM_CODEC = ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> INGREDIENTS_STREAM_CODEC = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());
        public static final MapCodec<ColoringRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ProcessingOutput.CODEC.listOf(1, 12).fieldOf("results").forGetter(ColoringRecipe::results),
                Ingredient.CODEC.listOf(1, 1).fieldOf("ingredients").forGetter(ColoringRecipe::ingredients),
                Identifier.CODEC.fieldOf("color").forGetter(ColoringRecipe::color),
                Codec.intRange(1, MAX_DYE_FLUID_AMOUNT)
                        .optionalFieldOf("dye_fluid_amount", DEFAULT_DYE_FLUID_AMOUNT)
                        .forGetter(ColoringRecipe::dyeFluidAmount))
                .apply(instance, ColoringRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ColoringRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public ColoringRecipe decode(RegistryFriendlyByteBuf buffer) {
                return new ColoringRecipe(
                        OUTPUTS_STREAM_CODEC.decode(buffer),
                        INGREDIENTS_STREAM_CODEC.decode(buffer),
                        Identifier.STREAM_CODEC.decode(buffer),
                        ByteBufCodecs.VAR_INT.decode(buffer));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ColoringRecipe recipe) {
                OUTPUTS_STREAM_CODEC.encode(buffer, recipe.results());
                INGREDIENTS_STREAM_CODEC.encode(buffer, recipe.ingredients());
                Identifier.STREAM_CODEC.encode(buffer, recipe.color());
                ByteBufCodecs.VAR_INT.encode(buffer, recipe.dyeFluidAmount());
            }
        };
        public static final RecipeSerializer<ColoringRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {}
    }
}
