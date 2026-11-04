package com.yision.fluidlogistics.content.fluids.powderSnow;


import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.neoforged.neoforge.fluids.FluidType;

public class PowderSnowFluidType extends FluidType {

	private final Identifier stillTexture;
	private final Identifier flowingTexture;

	public PowderSnowFluidType(Properties properties, Identifier stillTexture, Identifier flowingTexture) {
		super(properties);
		this.stillTexture = stillTexture;
		this.flowingTexture = flowingTexture;
	}

	@Override
	public BlockState getBlockForFluidState(net.minecraft.world.level.BlockAndLightGetter getter, BlockPos pos, FluidState state) {
		return Blocks.POWDER_SNOW.defaultBlockState();
	}

	public Identifier getStillTexture() {
		return stillTexture;
	}

	public Identifier getFlowingTexture() {
		return flowingTexture;
	}
}
