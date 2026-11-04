/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.neoforge;

import com.railwayteam.railways.fabric.RailwaysImpl;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Coordinates mod-bus listeners and eager Railways factories without modifying registry freeze state. */
public final class RailwaysNeoForgeInit {
    private static final List<Consumer<IEventBus>> MOD_BUS_TASKS = new ArrayList<>();
    private static final List<Runnable> CONTENT_TASKS = new ArrayList<>();
    private static volatile IEventBus modBus;
    private static boolean initializing;
    private static volatile boolean initialized;

    private RailwaysNeoForgeInit() {}

    public static void initialize() {
        if (initialized || initializing)
            return;
        if (modBus == null)
            throw new IllegalStateException("Railways mod bus must be bound before content registration");
        requireCreateContent();
        initializing = true;
        try {
            new RailwaysImpl().onInitialize();
        } finally {
            initializing = false;
        }
        // Publishing readiness and draining callbacks share the constructor callbacks' lock.
        List<Runnable> ready;
        synchronized (RailwaysNeoForgeInit.class) {
            initialized = true;
            ready = List.copyOf(CONTENT_TASKS);
            CONTENT_TASKS.clear();
        }
        ready.forEach(Runnable::run);
    }

    private static void requireCreateContent() {
        if (BuiltInRegistries.BLOCK.getValue(Identifier.parse("create:andesite_casing")) != AllBlocks.ANDESITE_CASING.get()
            || BuiltInRegistries.ITEM.getValue(Identifier.parse("create:wrench")) != AllItems.WRENCH.get()
            || BuiltInRegistries.FLUID.getValue(Identifier.parse("create:honey")) != AllFluids.HONEY.get().getSource())
            throw new IllegalStateException("Create Adapted must finish block, fluid and item registration before Railways");
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void requireRegistrationPhase() {
        if (!initializing)
            throw new IllegalStateException("Railways content must be registered from the final mutable RegisterEvent");
    }

    public static synchronized void onContentReady(Runnable task) {
        Objects.requireNonNull(task);
        if (initialized)
            task.run();
        else
            CONTENT_TASKS.add(task);
    }

    public static synchronized void onModBus(Consumer<IEventBus> task) {
        Objects.requireNonNull(task);
        if (modBus != null)
            task.accept(modBus);
        else
            MOD_BUS_TASKS.add(task);
    }

    public static synchronized void bindModBus(IEventBus bus) {
        if (modBus != null && modBus != bus)
            throw new IllegalStateException("Railways received two different mod event buses");
        modBus = bus;
        List<Consumer<IEventBus>> queued = List.copyOf(MOD_BUS_TASKS);
        MOD_BUS_TASKS.clear();
        queued.forEach(task -> task.accept(bus));
    }
}
