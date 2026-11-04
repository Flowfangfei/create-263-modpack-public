/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.client;

import net.createmod.ponder.api.client.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import plus.dragons.createdragonsplus.client.model.CDPPartialModels;
import plus.dragons.createdragonsplus.client.ponder.CDPPonderPlugin;
import plus.dragons.createdragonsplus.common.CDPCommon;

@Mod(value = CDPCommon.ID, dist = net.neoforged.api.distmarker.Dist.CLIENT)
public class CDPClient {
    public CDPClient(IEventBus modBus) {
        modBus.register(this);
        CDPPartialModels.register();
        modBus.addListener(this::registerFluidModels);
        modBus.addListener(this::registerFluidExtensions);
    }

    @SubscribeEvent
    public void setup(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CDPPonderPlugin());
    }
    public void registerFluidModels(net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event) {
        net.minecraft.core.registries.BuiltInRegistries.FLUID.forEach(fluid -> {
            if (fluid instanceof net.neoforged.neoforge.fluids.BaseFlowingFluid.Source source
                    && fluid.getFluidType() instanceof plus.dragons.createdragonsplus.common.fluids.SolidRenderFluidType type)
                event.register(plus.dragons.createdragonsplus.client.fluids.SolidFluidRendering.createModel(type), source, source.getFlowing());
        });
    }
    public void registerFluidExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        var seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<net.neoforged.neoforge.fluids.FluidType, Boolean>());
        net.minecraft.core.registries.BuiltInRegistries.FLUID.forEach(fluid -> {
            if (fluid.getFluidType() instanceof plus.dragons.createdragonsplus.common.fluids.SolidRenderFluidType type && seen.add(type))
                event.registerFluidType(plus.dragons.createdragonsplus.client.fluids.SolidFluidRendering.extensions(type), type);
        });
    }
}
