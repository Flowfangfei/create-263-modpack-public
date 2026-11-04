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

import com.zurrtum.create.content.fluids.pipes.VanillaFluidTargets;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.fluid.DragonBreathCauldronBlock;
import plus.dragons.createdragonsplus.fluid.FluidUnits;
import plus.dragons.createdragonsplus.registry.CDPBlocks;
import plus.dragons.createdragonsplus.registry.CDPFluids;

@Mixin(VanillaFluidTargets.class)
public abstract class VanillaFluidTargetsMixin {
    @Inject(method = "canProvideFluidWithoutCapability", at = @At("HEAD"), cancellable = true)
    private static void createDragonsPlus$recognizeDragonBreathCauldron(
            BlockState state, CallbackInfoReturnable<Boolean> callback) {
        if (state.is(CDPBlocks.DRAGON_BREATH_CAULDRON.get())) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "drainBlock", at = @At("HEAD"), cancellable = true)
    private static void createDragonsPlus$drainDragonBreathCauldron(
            Level level,
            BlockPos pos,
            BlockState state,
            boolean simulate,
            CallbackInfoReturnable<FluidStack> callback) {
        if (!state.is(CDPBlocks.DRAGON_BREATH_CAULDRON.get())) {
            return;
        }
        if (state.getValue(DragonBreathCauldronBlock.LEVEL) != DragonBreathCauldronBlock.MAX_LEVEL) {
            callback.setReturnValue(FluidStack.EMPTY);
            return;
        }
        if (!simulate) {
            level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
        }
        callback.setReturnValue(
                new FluidStack(CDPFluids.DRAGON_BREATH.getSource(), Math.toIntExact(FluidUnits.BUCKET)));
    }
}
