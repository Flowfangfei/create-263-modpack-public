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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.fluid.CDPFluidReactions;

/** Makes concrete powder solidify to the color represented by an adjacent dye fluid. */
@Mixin(ConcretePowderBlock.class)
public abstract class ConcretePowderBlockMixin {
    @Shadow
    @Final
    private Block concrete;

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void createDragonsPlus$solidifyFromDyeFluid(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random,
            CallbackInfoReturnable<BlockState> callback) {
        BlockState result = createDragonsPlus$solidifiedState(level, pos, concrete);
        if (result != null) {
            callback.setReturnValue(result);
        }
    }

    @Inject(method = "onLand", at = @At("HEAD"), cancellable = true)
    private void createDragonsPlus$solidifyAfterFalling(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockState replacedState,
            FallingBlockEntity fallingBlock,
            CallbackInfo callback) {
        BlockState result = createDragonsPlus$solidifiedState(level, pos, concrete);
        if (result != null) {
            level.setBlock(pos, result, Block.UPDATE_ALL);
            callback.cancel();
        }
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void createDragonsPlus$solidifyWhenPlaced(
            BlockPlaceContext context, CallbackInfoReturnable<BlockState> callback) {
        BlockState result = createDragonsPlus$solidifiedState(
                context.getLevel(), context.getClickedPos(), concrete);
        if (result != null) {
            callback.setReturnValue(result);
        }
    }

    @Unique
    private static @Nullable BlockState createDragonsPlus$solidifiedState(
            BlockGetter level, BlockPos pos, Block fallback) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            var variant = CDPFluidReactions.dyeVariant(
                    level.getFluidState(pos.relative(direction)).getType());
            if (variant == null) {
                continue;
            }
            Block colored = BuiltInRegistries.BLOCK.getValue(variant.concreteBlockId());
            return (colored == Blocks.AIR ? fallback : colored).defaultBlockState();
        }
        return null;
    }
}
