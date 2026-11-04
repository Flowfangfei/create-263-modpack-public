package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimPlatformService;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.FMLEnvironment;

public final class FabricSimPlatformService implements SimPlatformService {
    @Override
    public boolean isLoaded(final String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
