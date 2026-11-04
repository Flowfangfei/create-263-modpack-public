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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.fluid.CDPFluidReactions;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin {
    @Shadow
    @Final
    protected FlowingFluid fluid;

    @Inject(method = "shouldSpreadLiquid", at = @At("HEAD"), cancellable = true)
    private void createDragonsPlus$handleLavaReaction(
            Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> callback) {
        if (!fluid.is(FluidTags.LAVA)) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockState result = CDPFluidReactions.resultForFluid(
                    level.getFluidState(pos.relative(direction)).getType());
            if (result != null) {
                CDPFluidReactions.placeResult(level, pos, result);
                callback.setReturnValue(false);
                return;
            }
        }
    }
}
