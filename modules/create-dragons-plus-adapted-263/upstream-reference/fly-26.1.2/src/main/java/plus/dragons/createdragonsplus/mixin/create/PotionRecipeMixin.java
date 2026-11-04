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

package plus.dragons.createdragonsplus.mixin.create;

import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.fluid.FluidUnits;
import plus.dragons.createdragonsplus.registry.CDPFluids;

@Mixin(PotionRecipe.class)
public abstract class PotionRecipeMixin {
    @Inject(method = "register", at = @At("TAIL"))
    private static void createDragonsPlus$addFluidDragonBreathRecipes(
            Map<Identifier, Recipe<?>> recipes, CallbackInfo callback) {
        if (!CDPConfig.features().generateAutomaticBrewingRecipeForDragonBreathFluid.get()) {
            return;
        }
        List<Map.Entry<Identifier, PotionRecipe>> originals = recipes.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof PotionRecipe)
                .map(entry -> Map.entry(entry.getKey(), (PotionRecipe) entry.getValue()))
                .filter(entry -> entry.getValue().ingredient().acceptsItem(Items.DRAGON_BREATH.builtInRegistryHolder()))
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .toList();
        List<Map.Entry<Identifier, Recipe<?>>> additions = new ArrayList<>();
        for (Map.Entry<Identifier, PotionRecipe> entry : originals) {
            PotionRecipe potion = entry.getValue();
            Identifier id = CreateDragonsPlus.id("potion_mixing/"
                    + entry.getKey().getNamespace()
                    + "/"
                    + entry.getKey().getPath()
                    + "_using_dragon_breath_fluid");
            MixingRecipe replacement = new MixingRecipe(
                    100,
                    List.of(),
                    List.of(potion.result().copy()),
                    HeatCondition.HEATED,
                    List.of(
                            potion.fluidIngredient(),
                            new FluidStackIngredient(
                                    CDPFluids.DRAGON_BREATH.getSource(),
                                    DataComponentPatch.EMPTY,
                                    Math.toIntExact(FluidUnits.QUARTER_BUCKET))),
                    List.of());
            additions.add(Map.entry(id, replacement));
        }
        additions.forEach(entry -> recipes.put(entry.getKey(), entry.getValue()));
    }
}
