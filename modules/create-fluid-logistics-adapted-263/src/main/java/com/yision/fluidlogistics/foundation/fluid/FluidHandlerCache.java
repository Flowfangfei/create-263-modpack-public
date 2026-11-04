package com.yision.fluidlogistics.foundation.fluid;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.ICapabilityProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;

public final class FluidHandlerCache {

	private @Nullable ICapabilityProvider<IFluidHandler> sided;
	private @Nullable ICapabilityProvider<IFluidHandler> unsided;
	private @Nullable BlockPos cachedPos;
	private @Nullable Direction cachedSide;

	public @Nullable IFluidHandler get(Level level, BlockPos pos, @Nullable Direction side) {
		ensureCaches(level, pos, side);
		ICapabilityProvider<IFluidHandler> sidedProvider = sided;
		IFluidHandler handler = sidedProvider == null ? null : sidedProvider.getCapability();
		if (handler != null) {
			return handler;
		}
		ICapabilityProvider<IFluidHandler> unsidedProvider = unsided;
		return unsidedProvider == null ? null : unsidedProvider.getCapability();
	}

	public void invalidate() {
		sided = null;
		unsided = null;
		cachedPos = null;
		cachedSide = null;
	}

	private void ensureCaches(Level level, BlockPos pos, @Nullable Direction side) {
		if (!pos.equals(cachedPos) || side != cachedSide) {
			invalidate();
			cachedPos = pos.immutable();
			cachedSide = side;
		}
		if (sided == null) {
			sided = createProvider(level, pos, side, true);
		}
		if (unsided == null) {
			unsided = createProvider(level, pos, null, false);
		}
	}

	private ICapabilityProvider<IFluidHandler> createProvider(Level level, BlockPos pos, @Nullable Direction side,
        boolean sidedProvider) {
        if (level instanceof ServerLevel serverLevel) {
            var cache = BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, serverLevel, pos.immutable(), side,
                () -> true, () -> {
                    if (sidedProvider) sided = null;
                    else unsided = null;
                });
            return ICapabilityProvider.of(() -> {
                var handler = cache.getCapability();
                return handler == null ? null : IFluidHandler.of(handler);
            });
        }
        return ICapabilityProvider.of(() -> {
            var handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
            return handler == null ? null : IFluidHandler.of(handler);
        });
    }
}
