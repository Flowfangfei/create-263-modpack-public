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

package plus.dragons.createdragonsplus.fluid;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.registry.CDPFanProcessingTypes;

public final class DyeLiquidBlock extends FluidBlock {
    private final DyeVariant variant;

    public DyeLiquidBlock(DyeVariant variant, FlowableFluid fluid, Properties properties) {
        super(fluid, properties);
        this.variant = variant;
    }

    public DyeVariant variant() {
        return variant;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!CDPFluidReactions.reactAt(level, pos, CDPFluidReactions.resultFor(variant))) {
            super.onPlace(state, level, pos, oldState, movedByPiston);
        }
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            Orientation orientation,
            boolean movedByPiston) {
        if (!CDPFluidReactions.reactAt(level, pos, CDPFluidReactions.resultFor(variant))) {
            super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        }
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean emerged) {
        if (level.isClientSide() || !level.getFluidState(pos).isSource()) {
            return;
        }
        var type = CDPFanProcessingTypes.COLORING.get(variant.id());
        if (type == null) {
            return;
        }
        boolean colored = false;
        if (entity instanceof ItemEntity item && CDPConfig.dyeFluid().dyeFluidBlockContactColorsItems.get()) {
            colored = type.applyContactColoring(item, level);
        } else if (entity instanceof LivingEntity living
                && CDPConfig.dyeFluid().dyeFluidBlockContactColorsLivingEntities.get()) {
                    colored = type.applyContactColoring(living, level);
                }
        if (colored && CDPConfig.dyeFluid().dyeFluidBlockContactConsumesSource.get()) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }
}
