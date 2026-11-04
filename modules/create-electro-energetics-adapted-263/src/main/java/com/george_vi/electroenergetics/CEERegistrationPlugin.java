package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.zurrtum.create.api.registry.CreateRegisterPlugin;

/** Register before vanilla builds its block state table, as required by Create Fly. */
public final class CEERegistrationPlugin implements CreateRegisterPlugin {
    @Override
    public void onBlockRegister() {
        CEEConfigs.register();
        CEERegistries.register();
        CEEBlocks.register();
        CEEFluids.registerBlocks();
    }

    @Override
    public void onFluidRegister() {
        CEEFluids.register();
    }
}
