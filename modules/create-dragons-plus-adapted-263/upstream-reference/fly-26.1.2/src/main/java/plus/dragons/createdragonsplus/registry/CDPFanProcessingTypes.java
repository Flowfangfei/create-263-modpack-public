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

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.common.kinetics.fan.coloring.ColoringFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingFanProcessingType;
import plus.dragons.createdragonsplus.common.kinetics.fan.sanding.SandingFanProcessingType;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;

public final class CDPFanProcessingTypes {
    public static final Map<Identifier, ColoringFanProcessingType> COLORING = new LinkedHashMap<>();
    public static FreezingFanProcessingType FREEZING;
    public static SandingFanProcessingType SANDING;
    public static EndingFanProcessingType ENDING;
    private static boolean registered;

    private CDPFanProcessingTypes() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        for (DyeVariant variant : DyeVariantRegistry.all()) {
            ColoringFanProcessingType type = register(variant.fanProcessingName(), new ColoringFanProcessingType(variant));
            COLORING.put(variant.id(), type);
        }
        FREEZING = register("freezing", new FreezingFanProcessingType());
        SANDING = register("sanding", new SandingFanProcessingType());
        ENDING = register("ending", new EndingFanProcessingType());
    }

    private static <T extends FanProcessingType> T register(String name, T type) {
        return Registry.register(CreateRegistries.FAN_PROCESSING_TYPE, CreateDragonsPlus.id(name), type);
    }
}
