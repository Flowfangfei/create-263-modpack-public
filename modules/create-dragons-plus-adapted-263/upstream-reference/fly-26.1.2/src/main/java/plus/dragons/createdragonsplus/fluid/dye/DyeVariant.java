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

package plus.dragons.createdragonsplus.fluid.dye;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public record DyeVariant(
        Identifier id,
        String serializedName,
        String displayName,
        int color,
        TagKey<Item> dyeItemTag,
        Identifier dyeItemId,
        Identifier concreteBlockId,
        @Nullable DyeColor vanillaColor,
        @Nullable String requiredModId) {
    public boolean isVanilla() {
        return "minecraft".equals(id.getNamespace());
    }

    public boolean isAvailable() {
        return requiredModId == null || FabricLoader.getInstance().isModLoaded(requiredModId);
    }

    public String fluidName() {
        return isVanilla() ? id.getPath() + "_dye" : id.getNamespace() + "_" + id.getPath() + "_dye";
    }

    public String fanProcessingName() {
        return "coloring_" + serializedName;
    }

    public ItemStack dyeItemStack() {
        Item item = BuiltInRegistries.ITEM.getValue(dyeItemId);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }
}
