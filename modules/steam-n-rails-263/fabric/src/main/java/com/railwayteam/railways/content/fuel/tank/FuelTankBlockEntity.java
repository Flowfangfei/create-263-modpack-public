/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.tank;

import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/** A Create Adapted tank; both its storage and NeoForge capability count millibuckets. */
public class FuelTankBlockEntity extends FluidTankBlockEntity {
    public FuelTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected SmartFluidTank createInventory() {
        return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return LiquidFuelTrainHandler.isFuelForTanks(stack.getFluid());
            }
        };
    }

    public ResourceHandler<FluidResource> getFluidResourceHandler() {
        return getFluidResourceCapability();
    }
}
