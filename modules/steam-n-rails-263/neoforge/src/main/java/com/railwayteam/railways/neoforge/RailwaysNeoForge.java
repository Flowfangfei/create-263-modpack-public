/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.neoforge;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.multiloader.fabric.PacketSetImpl;
import com.railwayteam.railways.registry.fabric.CRFluidsImpl;
import com.railwayteam.railways.registry.fabric.CRBlockEntitiesImpl;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import com.railwayteam.railways.util.fabric.RegistrationListeningImpl;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Binds listeners during construction; content is created in NeoForge's mutable registration phase. */
@Mod(Railways.MOD_ID)
public class RailwaysNeoForge {
    public RailwaysNeoForge(IEventBus modBus, ModContainer container) {
        RailwaysNeoForgeInit.bindModBus(modBus);
        CRConfigs.register(container, modBus);
        CRFluidsImpl.registerTypes(modBus);
        modBus.addListener(EventPriority.LOWEST, RailwaysNeoForge::registerContent);
        modBus.addListener(PacketSetImpl::registerPayloads);
        modBus.addListener(RegisterCapabilitiesEvent.class, event -> CRBlockEntitiesImpl.registerCapabilities(event));
        RailwaysGameTests.register(modBus);
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(RegistrationListeningImpl::flush));
    }

    private static void registerContent(RegisterEvent event) {
        // This inherited facade registers eagerly across several registries. Wait until Create's
        // deferred entries are all bound, while NeoForge still keeps every registry mutable.
        // GameData freezes them together only after the complete RegisterEvent pass.
        var lastRegistry = net.neoforged.neoforge.registries.GameData.getRegistrationOrder().stream()
            .reduce((previous, current) -> current).orElseThrow();
        if (event.getRegistryKey().identifier().equals(lastRegistry))
            RailwaysNeoForgeInit.initialize();
    }
}
