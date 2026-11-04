package com.yision.fluidlogistics.content.fluids.fluidPort;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;

public interface FluidPort {

	@Nullable
	IFluidHandler getFluidDisplayCapability(@Nullable Direction side);

	boolean blocksFluidPackagerPlacement(Direction side);
}
