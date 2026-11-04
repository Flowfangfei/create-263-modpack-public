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

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import plus.dragons.createdragonsplus.common.fluids.WaterAndLavaLoggedBlock;
import plus.dragons.createdragonsplus.common.fluids.WaterAndLavaLoggedBlock.ContainedFluid;

/** Preserves enum-based water/lava logging while Create contraptions assemble and disassemble. */
@Mixin(Contraption.class)
public abstract class ContraptionMixin {
    @ModifyArg(method = "removeBlocksFromWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 1), index = 1)
    private BlockState createDragonsPlus$leaveContainedFluid(
            BlockState replacement, @Local(name = "oldState") BlockState oldState) {
        if (oldState.getBlock() instanceof WaterAndLavaLoggedBlock
                && oldState.hasProperty(WaterAndLavaLoggedBlock.FLUID)) {
            ContainedFluid contained = oldState.getValue(WaterAndLavaLoggedBlock.FLUID);
            if (contained != ContainedFluid.EMPTY) {
                return contained.legacyBlock();
            }
        }
        return replacement;
    }

    @ModifyArg(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), index = 1)
    private BlockState createDragonsPlus$adoptFluidAtDestination(
            BlockState state,
            @Local(name = "targetPos") BlockPos targetPos,
            @Local(argsOnly = true) Level level) {
        return WaterAndLavaLoggedBlock.withFluid(level, state, targetPos);
    }
}
