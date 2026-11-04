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

package plus.dragons.createdragonsplus.common.fluids;

import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/** Block contract for retaining either water or lava in one enum property. */
public interface WaterAndLavaLoggedBlock extends BucketPickup, LiquidBlockContainer {
    EnumProperty<ContainedFluid> FLUID = EnumProperty.create("fluid", ContainedFluid.class);

    @Override
    default boolean canPlaceLiquid(
            @Nullable LivingEntity entity,
            BlockGetter level,
            BlockPos pos,
            BlockState state,
            Fluid fluid) {
        return state.getValue(FLUID) == ContainedFluid.EMPTY
                && (fluid == Fluids.WATER || fluid == Fluids.LAVA);
    }

    @Override
    default boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (state.getValue(FLUID) != ContainedFluid.EMPTY) {
            return false;
        }
        ContainedFluid contained = ContainedFluid.from(fluidState.getType());
        if (contained == ContainedFluid.EMPTY) {
            return false;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(FLUID, contained), 3);
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
        return true;
    }

    @Override
    default ItemStack pickupBlock(
            @Nullable LivingEntity entity, LevelAccessor level, BlockPos pos, BlockState state) {
        ContainedFluid contained = state.getValue(FLUID);
        if (contained == ContainedFluid.EMPTY) {
            return ItemStack.EMPTY;
        }
        level.setBlock(pos, state.setValue(FLUID, ContainedFluid.EMPTY), 3);
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
        return new ItemStack(contained == ContainedFluid.WATER ? Items.WATER_BUCKET : Items.LAVA_BUCKET);
    }

    @Override
    default Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }

    default FluidState fluidState(BlockState state) {
        return state.getValue(FLUID).fluidState();
    }

    default void updateFluid(LevelAccessor level, BlockState state, BlockPos pos) {
        Fluid fluid = state.getValue(FLUID).fluid();
        if (fluid != Fluids.EMPTY) {
            level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
        }
    }

    default BlockState withFluid(BlockState placementState, BlockPlaceContext context) {
        return withFluid(context.getLevel(), placementState, context.getClickedPos());
    }

    static BlockState withFluid(LevelAccessor level, BlockState placementState, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (placementState.isAir()) {
            return fluidState.isEmpty() ? placementState : fluidState.createLegacyBlock();
        }
        if (!(placementState.getBlock() instanceof WaterAndLavaLoggedBlock)
                || !placementState.hasProperty(FLUID)) {
            return placementState;
        }
        return placementState.setValue(FLUID, ContainedFluid.from(fluidState.getType()));
    }

    enum ContainedFluid implements StringRepresentable {
        EMPTY,
        WATER,
        LAVA;

        static ContainedFluid from(Fluid fluid) {
            if (fluid == Fluids.WATER) {
                return WATER;
            }
            if (fluid == Fluids.LAVA) {
                return LAVA;
            }
            return EMPTY;
        }

        Fluid fluid() {
            return switch (this) {
                case EMPTY -> Fluids.EMPTY;
                case WATER -> Fluids.WATER;
                case LAVA -> Fluids.LAVA;
            };
        }

        public FluidState fluidState() {
            return switch (this) {
                case EMPTY -> Fluids.EMPTY.defaultFluidState();
                case WATER -> Fluids.WATER.getSource(false);
                case LAVA -> Fluids.LAVA.getSource(false);
            };
        }

        public BlockState legacyBlock() {
            return fluidState().createLegacyBlock();
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
