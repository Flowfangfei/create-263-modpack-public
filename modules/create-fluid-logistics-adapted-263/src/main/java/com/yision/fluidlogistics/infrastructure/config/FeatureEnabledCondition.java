package com.yision.fluidlogistics.infrastructure.config;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

import net.neoforged.neoforge.common.conditions.ICondition;

public record FeatureEnabledCondition(Identifier feature) implements ICondition {
	public static final MapCodec<FeatureEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
		.group(Identifier.CODEC.fieldOf("feature").forGetter(FeatureEnabledCondition::feature))
		.apply(builder, FeatureEnabledCondition::new));

	@Override
	public boolean test(@NotNull IContext context) {
		return FeatureToggle.isEnabled(feature);
	}

	@Override
	public @NotNull MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
