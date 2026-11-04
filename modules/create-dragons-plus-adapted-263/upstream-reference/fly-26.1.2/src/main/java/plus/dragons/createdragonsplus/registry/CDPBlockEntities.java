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

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;
import plus.dragons.createdragonsplus.fluid.hatch.FluidHatchBlockEntity;

public final class CDPBlockEntities {
    private static BlockEntityType<FluidHatchBlockEntity> fluidHatch;
    public static final CDPRegistryEntry<BlockEntityType<FluidHatchBlockEntity>> FLUID_HATCH = new CDPRegistryEntry<>(CreateDragonsPlus.id("fluid_hatch"), () -> fluidHatch);

    private CDPBlockEntities() {}

    public static void register() {
        fluidHatch = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                FLUID_HATCH.getId(),
                FabricBlockEntityTypeBuilder.create(
                        FluidHatchBlockEntity::new, CDPBlocks.FLUID_HATCH.get())
                        .build());
    }
}
