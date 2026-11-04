/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric;

import com.railwayteam.railways.ModSetup;
import com.railwayteam.railways.neoforge.RailwaysNeoForgeInit;

/** Retains the two content groups as explicit NeoForge registration steps, with no Create Fly SPI. */
public final class RailwaysCreateRegisterPlugin {
    private static boolean blocksRegistered;
    private static boolean fluidsRegistered;

    public static void registerContent() {
        RailwaysNeoForgeInit.requireRegistrationPhase();
        if (blocksRegistered || fluidsRegistered)
            throw new IllegalStateException("Railways block/fluid registration was requested twice");
        ModSetup.registerBlocksEarly();
        blocksRegistered = true;
        ModSetup.registerFluidsEarly();
        fluidsRegistered = true;
    }

    public static void verifyEarlyRegistrationComplete() {
        if (!blocksRegistered || !fluidsRegistered)
            throw new IllegalStateException("Railways block/fluid registration is incomplete");
    }
}
