package com.tom.createores.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.Codec;

public record FluidIngredient(SizedFluidIngredient delegate) {
	public static final Codec<FluidIngredient> CODEC = SizedFluidIngredient.CODEC.xmap(FluidIngredient::new, FluidIngredient::delegate);

	public int getRequiredAmount() {
		return delegate.amount();
	}

	public List<FluidStack> getMatchingFluidStacks() {
		return delegate.ingredient().fluids().stream()
				.map(Holder::value)
				.map(fluid -> new FluidStack(fluid, delegate.amount()))
				.toList();
	}

	public static Optional<FluidIngredient> read(RegistryFriendlyByteBuf buffer) {
		if (buffer.readBoolean())
			return Optional.of(new FluidIngredient(SizedFluidIngredient.STREAM_CODEC.decode(buffer)));
		return Optional.empty();
	}

	public static void write(RegistryFriendlyByteBuf buffer, Optional<FluidIngredient> ing) {
		buffer.writeBoolean(ing.isPresent());
		if (ing.isPresent())
			SizedFluidIngredient.STREAM_CODEC.encode(buffer, ing.get().delegate());
	}

	public boolean test(FluidStack fluid) {
		return delegate.test(fluid);
	}

	public static FluidIngredient fromFluid(FlowingFluid fluid, int amount) {
		return new FluidIngredient(SizedFluidIngredient.of(fluid, amount));
	}

	public static FluidIngredient fromTag(TagKey<Fluid> tag, int amount) {
		List<Holder<Fluid>> fluids = StreamSupport.stream(BuiltInRegistries.FLUID.getTagOrEmpty(tag).spliterator(), false).toList();
		return new FluidIngredient(new SizedFluidIngredient(net.neoforged.neoforge.fluids.crafting.FluidIngredient.of(HolderSet.direct(fluids)), amount));
	}

	public static FluidIngredient fromFluidStack(FluidStack fluidStack) {
		return new FluidIngredient(new SizedFluidIngredient(net.neoforged.neoforge.fluids.crafting.FluidIngredient.of(fluidStack), fluidStack.getAmount()));
	}
}
