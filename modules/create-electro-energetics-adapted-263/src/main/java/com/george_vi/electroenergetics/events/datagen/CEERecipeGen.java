package com.george_vi.electroenergetics.events.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import java.util.concurrent.CompletableFuture;

public final class CEERecipeGen extends ResourceRecipeProvider {
    public CEERecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, "crafting", "stonecutting", "sequenced_assembly");
    }
}
