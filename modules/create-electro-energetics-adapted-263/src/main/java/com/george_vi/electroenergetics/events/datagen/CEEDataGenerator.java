package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CEEDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public final class CEEDataGenerator implements DataGeneratorEntrypoint {
    @Override public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider((output, registries) -> new CEEStaticResourcesProvider(output));
        pack.addProvider(CEEGeneratedEntriesProvider::new);
        pack.addProvider(CEERecipeGen::new);
        pack.addProvider(CEEMixingRecipeGen::new);
        pack.addProvider(CEECompactingRecipeGen::new);
        pack.addProvider(CEEMechanicalCraftingRecipeGen::new);
        pack.addProvider(CEEAdvancements::new);
    }
    @Override public void buildRegistry(RegistrySetBuilder builder) {
        builder.add(Registries.DAMAGE_TYPE, CEEDamageTypes::bootstrap);
    }
}
