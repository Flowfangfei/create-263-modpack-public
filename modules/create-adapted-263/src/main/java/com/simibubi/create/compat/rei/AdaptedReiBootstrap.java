package com.simibubi.create.compat.rei;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@Mod("create")
public final class AdaptedReiBootstrap {
	public AdaptedReiBootstrap() {
		NeoForge.EVENT_BUS.addListener(this::sync);
		if (FMLEnvironment.getDist() == Dist.CLIENT && ModList.get().isLoaded("roughlyenoughitems"))
			new AdaptedReiCompat();
	}

	private void sync(OnDatapackSyncEvent event) {
		event.sendRecipes(RecipeType.CRAFTING);
		event.sendRecipes(RecipeType.SMELTING);
		for (String path : List.of("rolling", "charging", "liquid_burning")) {
			Identifier typeId = Identifier.fromNamespaceAndPath("createaddition", path);
			if (BuiltInRegistries.RECIPE_TYPE.containsKey(typeId))
				event.sendRecipes(BuiltInRegistries.RECIPE_TYPE.getValue(typeId));
		}
	}
}
