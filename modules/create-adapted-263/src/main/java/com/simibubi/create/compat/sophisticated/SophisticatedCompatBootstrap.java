package com.simibubi.create.compat.sophisticated;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod("create")
public final class SophisticatedCompatBootstrap {
	public SophisticatedCompatBootstrap(IEventBus modEventBus) {
		ModList mods = ModList.get();
		if (mods.isLoaded("sophisticatedstorage") && mods.isLoaded("sophisticatedcore")) {
			new SophisticatedContraptionCompat(modEventBus);
			if (FMLEnvironment.getDist() == Dist.CLIENT)
				modEventBus.addListener(MovingStorageScreens::register);
		}
	}
}
