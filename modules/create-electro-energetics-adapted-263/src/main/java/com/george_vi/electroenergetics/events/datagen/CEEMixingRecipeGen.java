package com.george_vi.electroenergetics.events.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import java.util.concurrent.CompletableFuture;

public final class CEEMixingRecipeGen extends ResourceRecipeProvider {
    public CEEMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, "mixing");
    }
}
