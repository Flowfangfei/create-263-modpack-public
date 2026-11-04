package com.tom.createores.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Kind;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import mezz.jei.api.gui.builder.ITooltipBuilder;

public class BiomeTooltip {
	private static long lastBiomeChangeTime;
	private static int biomePage;

	public static void listBiomes(TagKey<Biome> tag, ITooltipBuilder tooltip) {
        boolean isShift = Minecraft.getInstance().options.keyShift.isDown();
        Registry<Biome> biomeReg = Minecraft.getInstance().getConnection()
                .registryAccess().lookupOrThrow(Registries.BIOME);

        List<Holder<Biome>> holders = java.util.stream.StreamSupport
                .stream(biomeReg.getTagOrEmpty(tag).spliterator(), false)
                .toList();

        int size = holders.size();
        Stream<Holder<Biome>> stream = holders.stream();
        Component page = null;

        if (size > 16) {
            if (!isShift && System.currentTimeMillis() - lastBiomeChangeTime > 2000) {
                biomePage++;
                if (biomePage * 16 >= size) biomePage = 0;
                lastBiomeChangeTime = System.currentTimeMillis();
            }

            page = Component.translatable("tooltip.coe.page",
                    biomePage + 1, (size + 15) / 16);
            stream = stream.skip(biomePage * 16).limit(16);
        }

        List<Component> components = stream
                .map(holder -> getBiomeId(biomeReg, holder))
                .map(id -> Component.translatable(
                        "biome." + id.getNamespace() + "." + id.getPath()))
                .collect(Collectors.toCollection(java.util.ArrayList::new));

        if (page != null) components.add(page);
        tooltip.addAll(components);
    }

    private static Identifier getBiomeId(Registry<Biome> biomeReg, Holder<Biome> h) {
		try {
			if(h.kind() == Kind.DIRECT) {
				return biomeReg.getKey(h.value());
			} else {
				return ((Holder.Reference<Biome>)h).key().identifier();
			}
		} catch (Exception e) {
			return Identifier.parse("null");
		}
	}

	public static void resetPage() {
		biomePage = 0;
		lastBiomeChangeTime = 0;
	}
}
