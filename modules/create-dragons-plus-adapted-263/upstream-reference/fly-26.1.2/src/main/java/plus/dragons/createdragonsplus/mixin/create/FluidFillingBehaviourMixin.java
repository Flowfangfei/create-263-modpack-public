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

import com.zurrtum.create.content.fluids.transfer.FluidFillingBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.fluid.CDPFluidReactions;
import plus.dragons.createdragonsplus.fluid.DragonBreathVaporization;
import plus.dragons.createdragonsplus.fluid.DyeFluidVaporization;
import plus.dragons.createdragonsplus.fluid.FluidUnits;

/** Extends hose-pulley placement vaporization to CDP fluids. */
@Mixin(FluidFillingBehaviour.class)
public abstract class FluidFillingBehaviourMixin {
    @ModifyVariable(method = "tryDeposit", at = @At(value = "STORE"), name = "evaporate")
    private boolean createDragonsPlus$detectCustomVaporization(
            boolean evaporate, Fluid fluid, BlockPos root, boolean simulate) {
        if (evaporate || !CDPFluidReactions.isCDPFluid(fluid)) {
            return evaporate;
        }
        Level level = ((FluidFillingBehaviour) (Object) this).getLevel();
        return level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, root);
    }

    @Inject(method = "tryDeposit", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/transfer/FluidFillingBehaviour;playEffect(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;Z)V"), cancellable = true)
    private void createDragonsPlus$vaporizeCustomFluid(
            Fluid fluid, BlockPos root, boolean simulate, CallbackInfoReturnable<Boolean> callback) {
        if (!CDPFluidReactions.isCDPFluid(fluid)) {
            return;
        }
        Level level = ((FluidFillingBehaviour) (Object) this).getLevel();
        if (!level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, root)) {
            return;
        }
        if (!simulate) {
            if (CDPFluidReactions.isDragonBreath(fluid)) {
                DragonBreathVaporization.vaporize(level, root, null, Math.toIntExact(FluidUnits.BUCKET));
            } else {
                DyeFluidVaporization.vaporize(level, root);
            }
        }
        callback.setReturnValue(true);
    }
}
