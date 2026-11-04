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
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.registry.CDPFluidEntry;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;

public final class CDPFluids {
    public static final Map<Identifier, CDPFluidEntry<FlowableFluid>> DYES_BY_VARIANT = new LinkedHashMap<>();
    private static final Map<Identifier, RegisteredFluid> INTERNAL = new LinkedHashMap<>();
    public static CDPFluidEntry<FlowableFluid> DRAGON_BREATH;
    private static boolean registered;

    private CDPFluids() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        for (DyeVariant variant : DyeVariantRegistry.all()) {
            RegisteredFluid fluid = registerPair(variant.fluidName());
            DYES_BY_VARIANT.put(variant.id(), fluid.handle());
        }
        DRAGON_BREATH = registerPair("dragon_breath").handle();
    }

    private static RegisteredFluid registerPair(String path) {
        Identifier id = CreateDragonsPlus.id(path);
        Identifier flowingId = id.withPrefix("flowing_");
        ResourceKey<Fluid> sourceKey = ResourceKey.create(Registries.FLUID, id);
        ResourceKey<Fluid> flowingKey = ResourceKey.create(Registries.FLUID, flowingId);
        FluidEntry entry = new FluidEntry();
        entry.still = Registry.register(BuiltInRegistries.FLUID, sourceKey, new FlowableFluid.Still(entry));
        entry.flowing = Registry.register(BuiltInRegistries.FLUID, flowingKey, new FlowableFluid.Flowing(entry));
        CDPFluidEntry<FlowableFluid> handle = new CDPFluidEntry<>(id, entry.still, entry.flowing, () -> entry.bucket, () -> entry.block);
        RegisteredFluid registeredFluid = new RegisteredFluid(entry, handle);
        INTERNAL.put(id, registeredFluid);
        return registeredFluid;
    }

    static FluidEntry internal(CDPFluidEntry<FlowableFluid> fluid) {
        return internal(fluid.getId());
    }

    static FluidEntry internal(Identifier id) {
        RegisteredFluid value = INTERNAL.get(id);
        if (value == null) {
            throw new IllegalStateException("Unknown CDP fluid: " + id);
        }
        return value.entry();
    }

    public static Map<Identifier, CDPFluidEntry<FlowableFluid>> dyes() {
        return Collections.unmodifiableMap(DYES_BY_VARIANT);
    }

    private record RegisteredFluid(FluidEntry entry, CDPFluidEntry<FlowableFluid> handle) {}
}
