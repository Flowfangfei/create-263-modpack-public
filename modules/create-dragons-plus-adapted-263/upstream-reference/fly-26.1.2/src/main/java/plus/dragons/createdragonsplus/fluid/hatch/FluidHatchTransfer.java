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

package plus.dragons.createdragonsplus.fluid.hatch;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import plus.dragons.createdragonsplus.registry.CDPBlocks;

public final class FluidHatchTransfer {
    private FluidHatchTransfer() {}

    public static void register() {
        FluidStorage.SIDED.registerForBlocks(
                (level, pos, state, blockEntity, side) -> {
                    Direction facing = state.getValue(FluidHatchBlock.FACING);
                    if (side != null && side != facing.getOpposite()) {
                        return null;
                    }
                    BlockPos targetPos = pos.relative(facing);
                    var targetState = level.getBlockState(targetPos);
                    if (targetState.is(CDPBlocks.FLUID_HATCH.get())) {
                        return null;
                    }
                    return FluidStorage.SIDED.find(
                            level,
                            targetPos,
                            targetState,
                            level.getBlockEntity(targetPos),
                            facing.getOpposite());
                },
                CDPBlocks.FLUID_HATCH.get());
    }
}
