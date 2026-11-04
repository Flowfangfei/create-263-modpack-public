package com.yision.fluidlogistics.migration.mountedStorage;

import java.util.ArrayList;
import java.util.List;

import com.yision.fluidlogistics.content.fluids.horizontalMultiFluidTank.storage.HorizontalMultiFluidTankMountedStorage;
import com.yision.fluidlogistics.content.fluids.multiFluidTank.storage.MultiFluidTankMountedStorage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import net.neoforged.neoforge.fluids.FluidStack;

public final class LegacyMountedFluidStorageMigration {
	private LegacyMountedFluidStorageMigration() {
	}

	public static List<FluidStack> readFluids(HolderLookup.Provider registries, CompoundTag nbt, int tanks) {
		List<FluidStack> fluids = new ArrayList<>();
		CompoundTag fluidsNbt = nbt.getCompoundOrEmpty("Fluids");
		for (int i = 0; i < tanks; i++) {
			if (fluidsNbt.contains(Integer.toString(i))) {
				FluidStack fluid = com.yision.fluidlogistics.foundation.FluidLogisticsNbt.fluid(registries, fluidsNbt.getCompoundOrEmpty(Integer.toString(i)));
				fluids.add(fluid);
			} else {
				fluids.add(FluidStack.EMPTY);
			}
		}
		return fluids;
	}

	public static int readTanks(CompoundTag nbt) {
		return nbt.contains("Tanks") ? nbt.getIntOr("Tanks", 0) : 8;
	}

	public static MultiFluidTankMountedStorage readVertical(HolderLookup.Provider registries, CompoundTag nbt) {
		int capacity = nbt.getIntOr("Capacity", 0);
		int tanks = readTanks(nbt);
		List<FluidStack> fluids = readFluids(registries, nbt, tanks);
		return new MultiFluidTankMountedStorage(capacity, tanks, fluids);
	}

	public static HorizontalMultiFluidTankMountedStorage readHorizontal(
		HolderLookup.Provider registries, CompoundTag nbt) {
		int capacity = nbt.getIntOr("Capacity", 0);
		int tanks = readTanks(nbt);
		List<FluidStack> fluids = readFluids(registries, nbt, tanks);
		return new HorizontalMultiFluidTankMountedStorage(capacity, tanks, fluids);
	}
}
