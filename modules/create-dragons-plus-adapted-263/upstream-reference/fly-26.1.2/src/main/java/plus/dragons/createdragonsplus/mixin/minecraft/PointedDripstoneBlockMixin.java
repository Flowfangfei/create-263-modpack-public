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

package plus.dragons.createdragonsplus.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.registry.CDPFluids;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneBlockMixin {
    @ModifyExpressionValue(method = "maybeTransferFluid", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/material/Fluids;LAVA:Lnet/minecraft/world/level/material/FlowingFluid;"))
    private static FlowingFluid createDragonsPlus$recognizeDragonBreath(
            FlowingFluid original, @Local Fluid fluid) {
        return fluid == CDPFluids.DRAGON_BREATH.getSource()
                && CDPConfig.features().dragonBreathFluid.get()
                && CDPConfig.features().dragonBreathFluidDripstoneDuplication.get()
                        ? CDPFluids.DRAGON_BREATH.getSource()
                        : original;
    }

    @Inject(method = "canFillCauldron", at = @At("HEAD"), cancellable = true)
    private static void createDragonsPlus$canFillDragonBreathCauldron(
            Fluid fluid, CallbackInfoReturnable<Boolean> callback) {
        if (fluid == CDPFluids.DRAGON_BREATH.getSource()
                && CDPConfig.features().dragonBreathFluid.get()
                && CDPConfig.features().dragonBreathFluidDripstoneDuplication.get()) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "getDripParticle", at = @At("HEAD"), cancellable = true)
    private static void createDragonsPlus$dragonBreathDripParticle(
            Level level, Fluid fluid, BlockPos pos, CallbackInfoReturnable<ParticleOptions> callback) {
        if (fluid == CDPFluids.DRAGON_BREATH.getSource()) {
            callback.setReturnValue(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F));
        }
    }
}
