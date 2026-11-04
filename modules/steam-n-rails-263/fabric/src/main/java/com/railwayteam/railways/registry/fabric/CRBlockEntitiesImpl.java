/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlockEntity;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.railwayteam.railways.internal.compat.registrate.util.entry.BlockEntityEntry;
import com.railwayteam.railways.neoforge.RailwaysNeoForgeInit;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** NeoForge block entities and transaction-aware fluid providers, with amounts measured in mB. */
public final class CRBlockEntitiesImpl {
    public static final BlockEntityEntry<FuelTankBlockEntity> FUEL_TANK = Railways.registrate()
        .blockEntity("fuel_tank", FuelTankBlockEntity::new)
        .validBlocks(CRBlocksImpl.FUEL_TANK)
        .register();

    public static final BlockEntityEntry<PortableFuelInterfaceBlockEntity> PORTABLE_FUEL_INTERFACE = Railways.registrate()
        .blockEntity("portable_fuel_interface", PortableFuelInterfaceBlockEntity::new)
        .validBlocks(CRBlocksImpl.PORTABLE_FUEL_INTERFACE)
        .register();

    private CRBlockEntitiesImpl() {}

    public static void init() {
        RailwaysNeoForgeInit.requireRegistrationPhase();
        // Resolve both entries during content registration, before the capability event is fired.
        java.util.Objects.requireNonNull(FUEL_TANK.get());
        java.util.Objects.requireNonNull(PORTABLE_FUEL_INTERFACE.get());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, FUEL_TANK.get(),
            (blockEntity, side) -> blockEntity.getFluidResourceHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, PORTABLE_FUEL_INTERFACE.get(),
            (blockEntity, side) -> blockEntity.getFluidResourceHandler());
    }
}
