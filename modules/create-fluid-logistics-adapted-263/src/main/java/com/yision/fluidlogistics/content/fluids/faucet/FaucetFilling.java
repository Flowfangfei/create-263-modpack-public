package com.yision.fluidlogistics.content.fluids.faucet;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.fluids.spout.FillingBySpout;
import com.yision.fluidlogistics.compat.CompatMods;
import com.yision.fluidlogistics.compat.createenchantmentindustry.CreateEnchantmentIndustryCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;

public final class FaucetFilling {

	private FaucetFilling() {
	}

	public static boolean canItemBeFilled(Level level, ItemStack stack) {
		if (FillingBySpout.canItemBeFilled(level, stack)) {
			return true;
		}

		return CompatMods.createEnchantmentIndustryLoaded()
			&& CreateEnchantmentIndustryCompat.canRepairItem(stack);
	}

	public static int getRequiredAmountForItem(Level level, ItemStack stack, FluidStack availableFluid) {
		int requiredAmount = FillingBySpout.getRequiredAmountForItem(level, stack, availableFluid);
		if (requiredAmount != -1) {
			return requiredAmount;
		}

		if (CompatMods.createEnchantmentIndustryLoaded()) {
			int repairAmount = CreateEnchantmentIndustryCompat.getRequiredRepairFluidAmount(level, stack,
				availableFluid);
			if (repairAmount > 0) {
				return repairAmount;
			}
		}

		return -1;
	}

	public static ItemStack fillItem(Level level, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
		int createRequired = FillingBySpout.getRequiredAmountForItem(level, stack, availableFluid.copy());
		if (createRequired != -1) {
			return FillingBySpout.fillItem(level, requiredAmount, stack, availableFluid);
		}

		if (CompatMods.createEnchantmentIndustryLoaded()) {
			return CreateEnchantmentIndustryCompat.repairItemWithFluid(level, requiredAmount, stack, availableFluid);
		}

		return ItemStack.EMPTY;
	}

	static void writeFluid(CompoundTag tag, HolderLookup.Provider registries, String key, FluidStack stack) {
		if (!stack.isEmpty()) {
			tag.put(key, com.yision.fluidlogistics.foundation.FluidLogisticsNbt.save(stack,registries));
		}
	}

	static void writeItem(CompoundTag tag, HolderLookup.Provider registries, String key, ItemStack stack) {
		if (!stack.isEmpty()) {
			tag.put(key, com.yision.fluidlogistics.foundation.FluidLogisticsNbt.save(stack,registries));
		}
	}

	static void writeDirection(CompoundTag tag, String key, @Nullable Direction direction) {
		if (direction != null) {
			tag.putInt(key, direction.get3DDataValue());
		}
	}

	static void writeBlockPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
		if (pos != null) {
			tag.putLong(key, pos.asLong());
		}
	}

	static FluidStack readFluid(CompoundTag tag, HolderLookup.Provider registries, String key) {
		return tag.contains(key) ? com.yision.fluidlogistics.foundation.FluidLogisticsNbt.fluid(registries,tag.getCompoundOrEmpty(key))
			: FluidStack.EMPTY;
	}

	static ItemStack readItem(CompoundTag tag, HolderLookup.Provider registries, String key) {
		return tag.contains(key) ? com.yision.fluidlogistics.foundation.FluidLogisticsNbt.item(registries,tag.getCompoundOrEmpty(key))
			: ItemStack.EMPTY;
	}

	static @Nullable Direction readDirection(CompoundTag tag, String key) {
		return tag.contains(key) ? Direction.from3DDataValue(tag.getIntOr(key, 0)) : null;
	}

	static @Nullable BlockPos readBlockPos(CompoundTag tag, String key) {
		return tag.contains(key) ? BlockPos.of(tag.getLongOr(key, 0L)) : null;
	}
}
