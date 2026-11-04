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

package plus.dragons.createdragonsplus.registry;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;
import plus.dragons.createdragonsplus.fluid.DragonBreathCauldronBlock;
import plus.dragons.createdragonsplus.fluid.DragonBreathLiquidBlock;
import plus.dragons.createdragonsplus.fluid.DyeLiquidBlock;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.fluid.hatch.FluidHatchBlock;

public final class CDPBlocks {
    private static final Identifier FLUID_HATCH_ID = CreateDragonsPlus.id("fluid_hatch");
    private static FluidHatchBlock fluidHatch;
    private static DragonBreathCauldronBlock dragonBreathCauldron;
    public static final CDPRegistryEntry<FluidHatchBlock> FLUID_HATCH = new CDPRegistryEntry<>(FLUID_HATCH_ID, () -> fluidHatch);
    public static final CDPRegistryEntry<DragonBreathCauldronBlock> DRAGON_BREATH_CAULDRON = new CDPRegistryEntry<>(
            CreateDragonsPlus.id("dragon_breath_cauldron"), () -> dragonBreathCauldron);
    private static boolean registered;

    private CDPBlocks() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        for (DyeVariant variant : DyeVariantRegistry.all()) {
            var fluid = CDPFluids.DYES_BY_VARIANT.get(variant.id());
            FluidBlock block = register(
                    fluid.getId(),
                    properties -> new DyeLiquidBlock(variant, (FlowableFluid) fluid.getSource(), properties),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.WATER));
            CDPFluids.internal(fluid).block = block;
        }
        var dragonBreath = CDPFluids.DRAGON_BREATH;
        FluidBlock dragonBreathBlock = register(
                dragonBreath.getId(),
                properties -> new DragonBreathLiquidBlock(
                        (FlowableFluid) dragonBreath.getSource(), properties),
                BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                        .mapColor(MapColor.COLOR_PURPLE)
                        .lightLevel(state -> 15));
        CDPFluids.internal(dragonBreath).block = dragonBreathBlock;

        dragonBreathCauldron = register(
                DRAGON_BREATH_CAULDRON.getId(),
                DragonBreathCauldronBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).mapColor(MapColor.COLOR_PURPLE));

        fluidHatch = register(
                FLUID_HATCH_ID,
                FluidHatchBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                        .mapColor(MapColor.COLOR_GRAY)
                        .noOcclusion());
    }

    private static <T extends Block> T register(
            Identifier id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }
}
