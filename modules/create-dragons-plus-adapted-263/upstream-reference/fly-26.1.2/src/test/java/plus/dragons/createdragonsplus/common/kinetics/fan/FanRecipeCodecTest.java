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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringRecipe;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingRecipe;

class FanRecipeCodecTest {
    private static RegistryOps<com.google.gson.JsonElement> registryOps;

    @BeforeAll
    static void bootstrapRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        for (var item : List.of(Items.PACKED_ICE, Items.ICE, Items.LIME_WOOL, Items.WHITE_WOOL)) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
            }
        }
        RegistryAccess.Frozen registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
    }

    @Test
    void simpleFanRecipeCodecRoundTrips() {
        var expected = new FreezingRecipe(
                List.of(new ProcessingOutput(Items.PACKED_ICE.getDefaultInstance())),
                List.of(Ingredient.of(Items.ICE)));
        var codec = FreezingRecipe.Serializer.INSTANCE.codec().codec();
        var json = codec
                .encodeStart(registryOps, expected)
                .getOrThrow();
        var decoded = codec.parse(registryOps, json).getOrThrow();

        assertEquals(expected.results(), decoded.results());
        assertEquals(expected.ingredient(), decoded.ingredient());
    }

    @Test
    void coloringCodecPreservesJsonContract() {
        var expected = new ColoringRecipe(
                List.of(new ProcessingOutput(Items.LIME_WOOL.getDefaultInstance())),
                List.of(Ingredient.of(Items.WHITE_WOOL)),
                Identifier.withDefaultNamespace("lime"),
                ColoringRecipe.DEFAULT_DYE_FLUID_AMOUNT);
        var json = ColoringRecipe.Serializer.CODEC
                .codec()
                .encodeStart(registryOps, expected)
                .getOrThrow();
        var decoded = ColoringRecipe.Serializer.CODEC
                .codec()
                .parse(registryOps, json)
                .getOrThrow();

        assertEquals(expected, decoded);
    }
}
