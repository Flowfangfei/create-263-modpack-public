package com.george_vi.electroenergetics.foundation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

/** Defers resolving client implementation methods until the physical client invokes them. */
public final class ClientExecution {
    private ClientExecution() {}

    public static void run(Supplier<Runnable> factory) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) factory.get().run();
    }
}
