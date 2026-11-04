package com.yision.fluidlogistics.api.factorygauge;

import java.util.function.Supplier;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public record FactoryGaugeType(
	Identifier id,
	Identifier resourceTypeId,
	Supplier<? extends Item> item,
	FactoryGaugeFilterResolver filterResolver) {

	public FactoryGaugeType {
		if (id == null || resourceTypeId == null || item == null || filterResolver == null)
			throw new IllegalArgumentException("factory gauge type definition must not contain nulls");
	}
}
