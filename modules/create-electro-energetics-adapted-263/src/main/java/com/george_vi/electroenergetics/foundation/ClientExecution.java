package com.george_vi.electroenergetics.foundation;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Supplier;

/** Defers resolving client implementation methods until the physical client invokes them. */
public final class ClientExecution {
    private ClientExecution() {}

    public static void run(Supplier<Runnable> factory) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) factory.get().run();
    }
}
