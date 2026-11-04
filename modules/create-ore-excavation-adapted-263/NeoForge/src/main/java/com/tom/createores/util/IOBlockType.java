package com.tom.createores.util;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

public enum IOBlockType implements StringRepresentable {
	ITEM_IN   (() -> Capabilities.Item.BLOCK),
	ITEM_OUT  (() -> Capabilities.Item.BLOCK),
	FLUID_IN  (() -> Capabilities.Fluid.BLOCK),
	FLUID_OUT (() -> Capabilities.Fluid.BLOCK),
	ENERGY_IN (() -> Capabilities.Energy.BLOCK),
	ENERGY_OUT(() -> Capabilities.Energy.BLOCK),
	;
	private final String name;
	private final Supplier<BlockCapability<?, Direction>> cap;

	private IOBlockType(Supplier<BlockCapability<?, Direction>> cap) {
		name = name().toLowerCase(Locale.ROOT);
		this.cap = cap;
	}

	public BlockCapability<?, Direction> getCap() {
		return cap.get();
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
