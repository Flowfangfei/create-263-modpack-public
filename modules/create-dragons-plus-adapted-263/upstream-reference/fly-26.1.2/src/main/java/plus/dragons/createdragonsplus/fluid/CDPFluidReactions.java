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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.registry.CDPFluids;

public final class CDPFluidReactions {
    private CDPFluidReactions() {}

    public static boolean reactAt(Level level, BlockPos pos, BlockState result) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.LAVA)) {
                placeResult(level, pos, result);
                return true;
            }
        }
        return false;
    }

    public static BlockState resultFor(DyeVariant variant) {
        if (!CDPConfig.features().dyeFluidsLavaInteractionGenerateColoredConcrete.get()) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        Block block = BuiltInRegistries.BLOCK.getValue(variant.concreteBlockId());
        return block == Blocks.AIR ? Blocks.COBBLESTONE.defaultBlockState() : block.defaultBlockState();
    }

    public static BlockState reaction(Fluid first, Fluid second) {
        if (first.is(FluidTags.LAVA)) {
            return resultForFluid(second);
        }
        if (second.is(FluidTags.LAVA)) {
            return resultForFluid(first);
        }
        return null;
    }

    public static BlockState resultForFluid(Fluid fluid) {
        if (isDragonBreath(fluid)) {
            return Blocks.END_STONE.defaultBlockState();
        }
        DyeVariant variant = dyeVariant(fluid);
        return variant == null ? null : resultFor(variant);
    }

    public static DyeVariant dyeVariant(Fluid fluid) {
        for (DyeVariant variant : DyeVariantRegistry.all()) {
            var entry = CDPFluids.DYES_BY_VARIANT.get(variant.id());
            if (entry != null && (fluid == entry.getSource() || fluid == entry.getFlowing())) {
                return variant;
            }
        }
        return null;
    }

    public static boolean isDragonBreath(Fluid fluid) {
        return fluid == CDPFluids.DRAGON_BREATH.getSource() || fluid == CDPFluids.DRAGON_BREATH.getFlowing();
    }

    public static boolean isCDPFluid(Fluid fluid) {
        return isDragonBreath(fluid) || dyeVariant(fluid) != null;
    }

    public static void placeResult(Level level, BlockPos pos, BlockState result) {
        level.setBlockAndUpdate(pos, result);
        level.levelEvent(1501, pos, 0);
    }
}
