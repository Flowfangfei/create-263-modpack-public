package dev.eriksonn.aeronautics.neoforge.content.fluids.levitite;

import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.LevititeBlendDummyInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public class LevititeBlendNeoForge extends BaseFlowingFluid.Source implements LevititeBlendDummyInterface {
	public LevititeBlendNeoForge(Properties properties) {
		super(properties);
	}

	@Override
	public void tick(net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState blockState, FluidState state) {
		super.tick(level, pos, blockState, state);
		LevititeBlendDummyInterface.super.levititeBlendTick(level, pos, state);
	}

	@Override
	public boolean isSource(FluidState fluidState) {
		return true;
	}

	@Override
	public int getAmount(FluidState fluidState) {
		return 8;
	}
}
