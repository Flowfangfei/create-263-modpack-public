package com.george_vi.electroenergetics.compat.computercraft;

import net.fabricmc.loader.api.FabricLoader;

public final class CCProxy {
    private CCProxy() {}

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("computercraft"))
            CEEComputerBehaviour.register();
    }
}
