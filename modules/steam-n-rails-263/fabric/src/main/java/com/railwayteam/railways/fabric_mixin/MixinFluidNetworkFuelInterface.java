/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlockEntity;
import net.createmod.catnip.api.data.Pair;
import net.createmod.catnip.api.math.BlockFace;
import com.simibubi.create.content.fluids.FluidNetwork;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.foundation.ICapabilityProvider;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = FluidNetwork.class, remap = false)
public abstract class MixinFluidNetworkFuelInterface {
    @Shadow
    private @Nullable ICapabilityProvider<IFluidHandler> source;

    @Shadow
    private Set<Pair<BlockFace, PipeConnection>> frontier;

    @Inject(method = "keepPortableFluidInterfaceEngaged", at = @At("HEAD"), cancellable = true)
    private void railways$keepFuelInterfaceEngaged(CallbackInfo ci) {
        if (source == null || !(source.getCapability() instanceof PortableFuelInterfaceBlockEntity.InterfaceFluidHandler handler)) {
            return;
        }
        if (!frontier.isEmpty()) {
            handler.keepAlive();
        }
        ci.cancel();
    }
}
