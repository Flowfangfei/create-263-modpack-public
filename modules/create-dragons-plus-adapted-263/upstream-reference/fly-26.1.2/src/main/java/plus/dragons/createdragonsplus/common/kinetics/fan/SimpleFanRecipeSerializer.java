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

package plus.dragons.createdragonsplus.common.kinetics.fan;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class SimpleFanRecipeSerializer {
    private static final StreamCodec<RegistryFriendlyByteBuf, List<ProcessingOutput>> OUTPUTS_STREAM_CODEC = ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> INGREDIENTS_STREAM_CODEC = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());

    private SimpleFanRecipeSerializer() {}

    public static <R extends CreateSingleStackRollableRecipe> RecipeSerializer<R> create(
            BiFunction<List<ProcessingOutput>, List<Ingredient>, R> factory) {
        MapCodec<R> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ProcessingOutput.CODEC.listOf(1, 12).fieldOf("results").forGetter(R::results),
                Ingredient.CODEC
                        .listOf(1, 1)
                        .fieldOf("ingredients")
                        .forGetter(recipe -> List.of(recipe.ingredient())))
                .apply(instance, factory));
        StreamCodec<RegistryFriendlyByteBuf, R> streamCodec = new StreamCodec<>() {
            @Override
            public R decode(RegistryFriendlyByteBuf buffer) {
                return factory.apply(OUTPUTS_STREAM_CODEC.decode(buffer), INGREDIENTS_STREAM_CODEC.decode(buffer));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, R recipe) {
                OUTPUTS_STREAM_CODEC.encode(buffer, recipe.results());
                INGREDIENTS_STREAM_CODEC.encode(buffer, List.of(recipe.ingredient()));
            }
        };
        return new RecipeSerializer<>(codec, streamCodec);
    }
}
