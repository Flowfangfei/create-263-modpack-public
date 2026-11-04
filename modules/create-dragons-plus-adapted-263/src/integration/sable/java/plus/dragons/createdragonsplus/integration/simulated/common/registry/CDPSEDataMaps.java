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

package plus.dragons.createdragonsplus.integration.simulated.common.registry;

import static plus.dragons.createdragonsplus.common.CDPCommon.REGISTRATE;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataMapProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import plus.dragons.createdragonsplus.common.CDPCommon;

public class CDPSEDataMaps {
    public static final DataMapType<Block, Block> BLOCK_INTERACTION_BLASTING = DataMapType
            .builder(CDPCommon.asResource("air_current_block_interaction/blasting"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();
    public static final DataMapType<Block, Block> BLOCK_INTERACTION_SMOKING = DataMapType
            .builder(CDPCommon.asResource("air_current_block_interaction/smoking"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();
    public static final DataMapType<Block, Block> BLOCK_INTERACTION_SPLASHING = DataMapType
            .builder(CDPCommon.asResource("air_current_block_interaction/splashing"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();
    public static final DataMapType<Block, Block> BLOCK_INTERACTION_HAUNTING = DataMapType
            .builder(CDPCommon.asResource("air_current_block_interaction/haunting"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();
    public static final DataMapType<Block, Block> BLOCK_INTERACTION_FREEZING = DataMapType
            .builder(CDPCommon.asResource("air_current_block_interaction/freezing"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();
    public static final DataMapType<Block, Block> BLOCK_INTERACTION_SANDING = DataMapType
            .builder(CDPCommon.asResource("air_current_block_interaction/sanding"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();
    public static final DataMapType<Block, Block> BLOCK_INTERACTION_ENDING = DataMapType
            .builder(CDPCommon.asResource("air_current_block_interaction/ending"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();
    public static final DataMapType<Block, Block> FRAGILE_FLUID_TANK_LAVA = DataMapType
            .builder(CDPCommon.asResource("fragile_fluid_tank/lava"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();
    public static final DataMapType<Block, Block> FRAGILE_FLUID_TANK_WATER = DataMapType
            .builder(CDPCommon.asResource("fragile_fluid_tank/water"), Registries.BLOCK, BuiltInRegistries.BLOCK.byNameCodec())
            .synced(BuiltInRegistries.BLOCK.byNameCodec(), true)
            .build();

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterDataMapTypesEvent.class, CDPSEDataMaps::register);
        REGISTRATE.addDataGenerator(ProviderType.DATA_MAP, CDPSEDataMaps::generate);
    }

    public static void register(final RegisterDataMapTypesEvent event) {
        event.register(BLOCK_INTERACTION_BLASTING);
        event.register(BLOCK_INTERACTION_SMOKING);
        event.register(BLOCK_INTERACTION_SPLASHING);
        event.register(BLOCK_INTERACTION_HAUNTING);
        event.register(BLOCK_INTERACTION_FREEZING);
        event.register(BLOCK_INTERACTION_SANDING);
        event.register(BLOCK_INTERACTION_ENDING);
        event.register(FRAGILE_FLUID_TANK_LAVA);
        event.register(FRAGILE_FLUID_TANK_WATER);
    }

    public static void generate(RegistrateDataMapProvider provider) {
        genLavaDataMap(provider, BLOCK_INTERACTION_BLASTING);
        genLavaDataMap(provider, FRAGILE_FLUID_TANK_LAVA);
        provider.builder(BLOCK_INTERACTION_SMOKING)
                .add(Blocks.SNOW.defaultBlockState().getBlock().builtInRegistryHolder(), Blocks.AIR, false);
        genWaterDataMap(provider, BLOCK_INTERACTION_SPLASHING);
        genWaterDataMap(provider, FRAGILE_FLUID_TANK_WATER);
        provider.builder(BLOCK_INTERACTION_FREEZING)
                .add(Blocks.WATER.defaultBlockState().getBlock().builtInRegistryHolder(), Blocks.ICE, false);
    }

    private static void genLavaDataMap(RegistrateDataMapProvider provider, DataMapType<Block, Block> lavaMap) {
        provider.builder(lavaMap)
                .add(Blocks.WET_SPONGE.builtInRegistryHolder(), Blocks.SPONGE, false)
                .add(Blocks.ICE.builtInRegistryHolder(), Blocks.WATER, false)
                .add(Blocks.SNOW.defaultBlockState().getBlock().builtInRegistryHolder(), Blocks.AIR, false)
                .add(Blocks.SNOW_BLOCK.defaultBlockState().getBlock().builtInRegistryHolder(), Blocks.AIR, false)
                .add(Blocks.POWDER_SNOW.defaultBlockState().getBlock().builtInRegistryHolder(), Blocks.AIR, false);
    }

    private static void genWaterDataMap(RegistrateDataMapProvider provider, DataMapType<Block, Block> waterMap) {
        provider.builder(waterMap)
                .add(Blocks.SPONGE.defaultBlockState().getBlock().builtInRegistryHolder(), Blocks.WET_SPONGE, false)
                .add(Blocks.WET_SPONGE.builtInRegistryHolder(), Blocks.SPONGE, false)
                .add(Blocks.CONCRETE_POWDER.white().builtInRegistryHolder(), Blocks.CONCRETE.white(), false)
                .add(Blocks.CONCRETE_POWDER.lightGray().builtInRegistryHolder(), Blocks.CONCRETE.lightGray(), false)
                .add(Blocks.CONCRETE_POWDER.gray().builtInRegistryHolder(), Blocks.CONCRETE.gray(), false)
                .add(Blocks.CONCRETE_POWDER.black().builtInRegistryHolder(), Blocks.CONCRETE.black(), false)
                .add(Blocks.CONCRETE_POWDER.brown().builtInRegistryHolder(), Blocks.CONCRETE.brown(), false)
                .add(Blocks.CONCRETE_POWDER.red().builtInRegistryHolder(), Blocks.CONCRETE.red(), false)
                .add(Blocks.CONCRETE_POWDER.yellow().builtInRegistryHolder(), Blocks.CONCRETE.yellow(), false)
                .add(Blocks.CONCRETE_POWDER.orange().builtInRegistryHolder(), Blocks.CONCRETE.orange(), false)
                .add(Blocks.CONCRETE_POWDER.lime().builtInRegistryHolder(), Blocks.CONCRETE.lime(), false)
                .add(Blocks.CONCRETE_POWDER.green().builtInRegistryHolder(), Blocks.CONCRETE.green(), false)
                .add(Blocks.CONCRETE_POWDER.cyan().builtInRegistryHolder(), Blocks.CONCRETE.cyan(), false)
                .add(Blocks.CONCRETE_POWDER.lightBlue().builtInRegistryHolder(), Blocks.CONCRETE.lightBlue(), false)
                .add(Blocks.CONCRETE_POWDER.blue().builtInRegistryHolder(), Blocks.CONCRETE.blue(), false)
                .add(Blocks.CONCRETE_POWDER.purple().builtInRegistryHolder(), Blocks.CONCRETE.purple(), false)
                .add(Blocks.CONCRETE_POWDER.magenta().builtInRegistryHolder(), Blocks.CONCRETE.magenta(), false)
                .add(Blocks.CONCRETE_POWDER.pink().builtInRegistryHolder(), Blocks.CONCRETE.pink(), false)
                .add(Blocks.WOOL.lightGray().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.gray().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.black().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.brown().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.red().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.orange().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.yellow().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.lime().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.green().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.cyan().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.blue().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.purple().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.magenta().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.WOOL.pink().builtInRegistryHolder(), Blocks.WOOL.white(), false)
                .add(Blocks.FIRE.builtInRegistryHolder(), Blocks.AIR, false);
    }
}
