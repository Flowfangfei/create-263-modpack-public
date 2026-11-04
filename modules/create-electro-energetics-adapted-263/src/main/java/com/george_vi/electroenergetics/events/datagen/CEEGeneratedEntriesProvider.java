package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CEEDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import java.util.concurrent.CompletableFuture;

public final class CEEGeneratedEntriesProvider extends FabricDynamicRegistryProvider {
    public CEEGeneratedEntriesProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    @Override protected void configure(HolderLookup.Provider registries, Entries entries) {
        var damage = registries.lookupOrThrow(Registries.DAMAGE_TYPE);
        entries.add(CEEDamageTypes.ELECTROCUTION, damage.getOrThrow(CEEDamageTypes.ELECTROCUTION).value());
        entries.add(CEEDamageTypes.HV_ELECTROCUTION, damage.getOrThrow(CEEDamageTypes.HV_ELECTROCUTION).value());
    }
    @Override public String getName() { return "CEE damage types"; }
}
