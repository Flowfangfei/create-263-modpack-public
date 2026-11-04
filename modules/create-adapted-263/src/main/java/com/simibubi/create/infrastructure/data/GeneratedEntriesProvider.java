package com.simibubi.create.infrastructure.data;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllDamageTypes;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileTypes;
import com.simibubi.create.infrastructure.worldgen.AllBiomeModifiers;
import com.simibubi.create.infrastructure.worldgen.AllConfiguredFeatures;
import com.simibubi.create.infrastructure.worldgen.AllPlacedFeatures;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.CachedOutput;

import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GeneratedEntriesProvider implements DataProvider {
	private final DatapackBuiltinEntriesProvider delegate;

	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
		.add(Registries.ENCHANTMENT, AllEnchantments::bootstrap)
		.add(Registries.DAMAGE_TYPE, AllDamageTypes::bootstrap)
		.add(Registries.FEATURE, AllConfiguredFeatures::bootstrap)
		.add(Registries.PLACED_FEATURE, AllPlacedFeatures::bootstrap)
		.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AllBiomeModifiers::bootstrap)
		.add(CreateRegistries.POTATO_PROJECTILE_TYPE, AllPotatoProjectileTypes::bootstrap);

	public GeneratedEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		delegate = DatapackBuiltinEntriesProvider.forWorldLayer(output, "Create registry entries", registries, BUILDER, Set.of(Create.ID));
	}

	public CompletableFuture<HolderLookup.Provider> getRegistryProvider() {
		return delegate.getRegistryProvider();
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return delegate.run(output);
	}

	@Override
	public String getName() {
		return "Create's Generated Registry Entries";
	}
}
