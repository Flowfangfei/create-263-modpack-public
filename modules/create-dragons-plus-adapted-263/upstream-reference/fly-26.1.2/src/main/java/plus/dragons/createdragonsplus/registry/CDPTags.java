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

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.CreateDragonsPlus;

public final class CDPTags {
    public static final TagKey<Block> PASSIVE_BLOCK_FREEZERS = block("passive_block_freezers");
    public static final TagKey<Block> NOT_APPLICABLE_POLISHING = block("not_applicable_for_polishing");
    public static final TagKey<Block> FAN_SANDING_CATALYSTS = block("fan_processing_catalysts/sanding");
    public static final TagKey<Block> FAN_ENDING_CATALYSTS = block("fan_processing_catalysts/ending");
    public static final TagKey<Fluid> FLUID_FAN_ENDING_CATALYSTS = fluid("fan_processing_catalysts/ending");
    public static final TagKey<Fluid> SWIMMABLE_FLUIDS = fluid("swimmable");
    public static final TagKey<Item> NOT_APPLICABLE_COLORING = item("not_applicable_for_coloring");

    private CDPTags() {}

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, CreateDragonsPlus.id(path));
    }

    private static TagKey<Fluid> fluid(String path) {
        return TagKey.create(Registries.FLUID, CreateDragonsPlus.id(path));
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, CreateDragonsPlus.id(path));
    }
}
