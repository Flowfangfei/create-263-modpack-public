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

import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinOperatingBlockEntity;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.registry.CDPFanProcessingTypes;
import plus.dragons.createdragonsplus.registry.CDPFluids;

@Mixin(BasinOperatingBlockEntity.class)
public abstract class BasinOperatingBlockEntityMixin {
    @Shadow
    protected abstract Optional<BasinBlockEntity> getBasin();

    @Shadow
    protected abstract boolean matchBasinRecipe(Recipe<?> recipe);

    @Inject(method = "getMatchingRecipes", at = @At("RETURN"), cancellable = true)
    private void createDragonsPlus$findDyeFluidMixingRecipe(CallbackInfoReturnable<Recipe<?>> callback) {
        if (callback.getReturnValue() != null
                || !((Object) this instanceof MechanicalMixerBlockEntity)
                || !CDPConfig.features().dyeFluids.get()) {
            return;
        }
        Optional<BasinBlockEntity> optionalBasin = getBasin();
        if (optionalBasin.isEmpty()) {
            return;
        }
        BasinBlockEntity basin = optionalBasin.get();
        Level level = basin.getLevel();
        if (level == null) {
            return;
        }
        for (DyeVariant variant : DyeVariantRegistry.all()) {
            var fluid = CDPFluids.DYES_BY_VARIANT.get(variant.id());
            if (fluid == null
                    || basin.fluidCapability.count(stack -> stack.isOf(fluid.getSource()))
                            .getAmount() <= 0) {
                continue;
            }
            var processingType = CDPFanProcessingTypes.COLORING.get(variant.id());
            if (processingType == null) {
                continue;
            }
            for (int slot = 0; slot < basin.itemCapability.getContainerSize(); slot++) {
                ItemStack input = basin.itemCapability.getItem(slot);
                if (input.isEmpty()) {
                    continue;
                }
                var recipe = processingType.createMixingRecipe(input, level);
                if (recipe.isPresent() && matchBasinRecipe(recipe.get())) {
                    callback.setReturnValue(recipe.get());
                    return;
                }
            }
        }
    }
}
