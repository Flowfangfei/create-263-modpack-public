/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceRenderer;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceMovementRender;
import com.railwayteam.railways.content.fuel.tank.FuelTankModel;
import com.railwayteam.railways.content.fuel.tank.FuelTankRenderer;
import com.railwayteam.railways.internal.compat.client.render.AllBlockEntityRenders;
import com.railwayteam.railways.internal.compat.client.model.AllModels;

/** Client-only fuel model and block-entity renderer wiring. */
public final class CRBlockEntitiesClientImpl {
    private CRBlockEntitiesClientImpl() {
    }

    public static void register() {
        com.simibubi.create.CreateClient.MODEL_SWAPPER.getCustomConnectedTextures().register(CRBlocksImpl.FUEL_TANK.get(),
            new com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour(
                com.railwayteam.railways.registry.CRSpriteShifts.FUEL_TANK,
                com.railwayteam.railways.registry.CRSpriteShifts.FUEL_TANK_TOP,
                com.railwayteam.railways.registry.CRSpriteShifts.FUEL_TANK_INNER));
        AllBlockEntityRenders.render(CRBlockEntitiesImpl.FUEL_TANK.get(), FuelTankRenderer::new);
        AllBlockEntityRenders.render(
            CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get(),
            PortableFuelInterfaceRenderer::new
        );
    }
}
