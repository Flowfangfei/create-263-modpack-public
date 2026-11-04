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

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import plus.dragons.createdragonsplus.CreateDragonsPlus;

public final class DyeColors {
    public static final DyeColor[] CREATIVE_ORDER = {
            DyeColor.WHITE,
            DyeColor.LIGHT_GRAY,
            DyeColor.GRAY,
            DyeColor.BLACK,
            DyeColor.BROWN,
            DyeColor.RED,
            DyeColor.ORANGE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.GREEN,
            DyeColor.CYAN,
            DyeColor.LIGHT_BLUE,
            DyeColor.BLUE,
            DyeColor.PURPLE,
            DyeColor.MAGENTA,
            DyeColor.PINK
    };
    private static final Map<DyeColor, String> DISPLAY_NAMES = new EnumMap<>(DyeColor.class);

    static {
        DISPLAY_NAMES.put(DyeColor.WHITE, "White");
        DISPLAY_NAMES.put(DyeColor.LIGHT_GRAY, "Light Gray");
        DISPLAY_NAMES.put(DyeColor.GRAY, "Gray");
        DISPLAY_NAMES.put(DyeColor.BLACK, "Black");
        DISPLAY_NAMES.put(DyeColor.BROWN, "Brown");
        DISPLAY_NAMES.put(DyeColor.RED, "Red");
        DISPLAY_NAMES.put(DyeColor.ORANGE, "Orange");
        DISPLAY_NAMES.put(DyeColor.YELLOW, "Yellow");
        DISPLAY_NAMES.put(DyeColor.LIME, "Lime");
        DISPLAY_NAMES.put(DyeColor.GREEN, "Green");
        DISPLAY_NAMES.put(DyeColor.CYAN, "Cyan");
        DISPLAY_NAMES.put(DyeColor.LIGHT_BLUE, "Light Blue");
        DISPLAY_NAMES.put(DyeColor.BLUE, "Blue");
        DISPLAY_NAMES.put(DyeColor.PURPLE, "Purple");
        DISPLAY_NAMES.put(DyeColor.MAGENTA, "Magenta");
        DISPLAY_NAMES.put(DyeColor.PINK, "Pink");
    }

    private DyeColors() {}

    static void registerVanilla(DyeVariantRegistry.Builder builder) {
        for (DyeColor color : CREATIVE_ORDER) {
            String name = color.getSerializedName();
            builder.add(new DyeVariant(
                    Identifier.withDefaultNamespace(name),
                    name,
                    DISPLAY_NAMES.get(color),
                    color.getTextureDiffuseColor(),
                    TagKey.create(Registries.ITEM, CreateDragonsPlus.id("dyes/minecraft/" + name)),
                    Identifier.withDefaultNamespace(name + "_dye"),
                    Identifier.withDefaultNamespace(name + "_concrete"),
                    color,
                    null));
        }
    }
}
