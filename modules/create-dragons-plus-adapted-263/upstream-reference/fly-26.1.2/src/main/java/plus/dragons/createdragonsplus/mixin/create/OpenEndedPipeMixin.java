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

import com.zurrtum.create.content.fluids.OpenEndedPipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.fluid.CDPFluidReactions;
import plus.dragons.createdragonsplus.fluid.DragonBreathVaporization;
import plus.dragons.createdragonsplus.fluid.DyeFluidVaporization;

@Mixin(OpenEndedPipe.class)
public abstract class OpenEndedPipeMixin {
    @Shadow
    private Level world;

    @Shadow
    @Final
    private BlockPos outputPos;

    @Inject(method = "provideFluidToSpace", at = @At("HEAD"), cancellable = true)
    private void createDragonsPlus$handleCustomFluid(
            FluidStack stack, boolean simulate, CallbackInfoReturnable<Boolean> callback) {
        if (world == null || stack.isEmpty()) {
            return;
        }
        Fluid fluid = stack.getFluid();
        if (!CDPFluidReactions.isCDPFluid(fluid)) {
            return;
        }
        if (world.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, outputPos)) {
            if (!simulate) {
                if (CDPFluidReactions.isDragonBreath(fluid)) {
                    DragonBreathVaporization.vaporize(world, outputPos, null, stack.getAmount());
                } else {
                    DyeFluidVaporization.vaporize(world, outputPos);
                }
            }
            callback.setReturnValue(true);
            return;
        }
        BlockState reaction = CDPFluidReactions.reaction(fluid, world.getFluidState(outputPos).getType());
        if (reaction != null) {
            if (!simulate) {
                CDPFluidReactions.placeResult(world, outputPos, reaction);
            }
            callback.setReturnValue(true);
        }
    }
}
