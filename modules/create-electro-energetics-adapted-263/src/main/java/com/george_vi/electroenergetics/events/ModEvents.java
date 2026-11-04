package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.events.datagen.CEETriggers;
import com.george_vi.electroenergetics.events.recipe.FluidCompactingRecipe;

public final class ModEvents {
    private ModEvents() {}
    public static void register() {
        RegistryRemaps.register();
        CEETriggers.register();
        FluidCompactingRecipe.register();
    }
}
