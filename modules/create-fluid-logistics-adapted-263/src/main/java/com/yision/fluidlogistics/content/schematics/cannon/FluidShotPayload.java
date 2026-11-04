package com.yision.fluidlogistics.content.schematics.cannon;

import java.util.UUID;

import com.yision.fluidlogistics.content.schematics.FluidSchematicPlan.Cell;
import com.yision.fluidlogistics.content.schematics.FluidSchematicPlan.Kind;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public final class FluidShotPayload {

	public static final String KEY = "fluidlogistics:fluid_shot";

	public record Data(
		UUID jobId,
		int planHash,
		int cellIndex,
		Kind kind,
		Fluid fluid,
		boolean fluidConsumed,
		Block expectedHost) {

		public FluidStack requiredFluid() {
			return new FluidStack(fluid, FluidType.BUCKET_VOLUME);
		}
	}

	private FluidShotPayload() {
	}

	public static CompoundTag create(UUID jobId, int planHash, Cell cell, boolean fluidConsumed) {
		CompoundTag shot = new CompoundTag();
		shot.store("Job",net.minecraft.core.UUIDUtil.CODEC,jobId);
		shot.putInt("PlanHash", planHash);
		shot.putInt("Cell", cell.index());
		shot.putString("Kind", cell.kind().name());
		shot.putString("Fluid", BuiltInRegistries.FLUID.getKey(cell.fluid().getFluid()).toString());
		shot.putBoolean("FluidConsumed", fluidConsumed);
		shot.putString("Host", BuiltInRegistries.BLOCK.getKey(cell.expectedHost()).toString());
		CompoundTag data = new CompoundTag();
		data.put(KEY, shot);
		return data;
	}

	public static boolean isFluidShot(CompoundTag data) {
		return data != null && data.contains(KEY);
	}

	public static Data read(CompoundTag data) {
		if (!isFluidShot(data)) {
			return null;
		}
		CompoundTag shot = data.getCompoundOrEmpty(KEY);
		if (!shot.read("Job",net.minecraft.core.UUIDUtil.CODEC).isPresent()) {
			return null;
		}
		try {
			Kind kind = Kind.valueOf(shot.getStringOr("Kind", ""));
			Fluid fluid = BuiltInRegistries.FLUID
				.getOptional(Identifier.parse(shot.getStringOr("Fluid", ""))).orElse(null);
			Block host = BuiltInRegistries.BLOCK
				.getOptional(Identifier.parse(shot.getStringOr("Host", ""))).orElse(null);
			if (fluid == null || fluid == Fluids.EMPTY || host == null) {
				return null;
			}
			return new Data(
				shot.read("Job",net.minecraft.core.UUIDUtil.CODEC).orElseThrow(),
				shot.getIntOr("PlanHash", 0),
				shot.getIntOr("Cell", 0),
				kind,
				fluid,
				shot.getBooleanOr("FluidConsumed", false),
				host);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}
}
