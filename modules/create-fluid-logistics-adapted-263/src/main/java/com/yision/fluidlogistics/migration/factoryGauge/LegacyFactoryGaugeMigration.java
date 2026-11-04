package com.yision.fluidlogistics.migration.factoryGauge;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.yision.fluidlogistics.FluidLogistics;
import com.yision.fluidlogistics.api.packager.PackageResourceTypes;
import com.yision.fluidlogistics.api.packager.PackageResources;
import com.yision.fluidlogistics.content.logistics.factoryGauge.ResourceFactoryGaugeNbt;
import com.yision.fluidlogistics.content.logistics.factoryGauge.ResourceGaugeHelper;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class LegacyFactoryGaugeMigration {
	public static final String KIND_KEY = "CFLGaugeKind";
	public static final String FLUID_KIND = "fluid";

	private LegacyFactoryGaugeMigration() {
	}

	@Nullable
	public static Identifier determineTypeId(CompoundTag rawSlotTag, HolderLookup.Provider registries) {
		if (FLUID_KIND.equals(rawSlotTag.getString(KIND_KEY))) {
			return FluidLogistics.asResource("fluid");
		}

		ItemStack legacyFilter = FilterItemStack.of(registries, rawSlotTag.getCompoundOrEmpty("Filter")).item();
		if (!legacyFilter.isEmpty() && PackageResources.findType(legacyFilter)
			.map(type -> type.id().equals(PackageResourceTypes.FLUID))
			.orElse(false)) {
			return FluidLogistics.asResource("fluid");
		}

		return null;
	}

	public static CompoundTag readSettings(CompoundTag rawSlotTag) {
		CompoundTag stateTag = rawSlotTag.getCompoundOrEmpty(ResourceFactoryGaugeNbt.STATE_KEY).copy();
		if (rawSlotTag.contains(ResourceGaugeHelper.RESTOCK_THRESHOLD_KEY)) {
			stateTag.putInt("RestockThreshold", rawSlotTag.getIntOr(ResourceGaugeHelper.RESTOCK_THRESHOLD_KEY, 0));
		}
		if (rawSlotTag.contains(ResourceGaugeHelper.PROMISE_LIMIT_KEY)) {
			stateTag.putInt("PromiseLimit", rawSlotTag.getIntOr(ResourceGaugeHelper.PROMISE_LIMIT_KEY, 0));
		}
		if (rawSlotTag.contains(ResourceGaugeHelper.ADDITIONAL_STOCK_KEY)) {
			stateTag.putInt("AdditionalStock", rawSlotTag.getIntOr(ResourceGaugeHelper.ADDITIONAL_STOCK_KEY, 0));
		}
		if (!stateTag.contains("RemainingAdditionalStock")
			&& rawSlotTag.contains(ResourceGaugeHelper.REMAINING_ADDITIONAL_STOCK_KEY)) {
			stateTag.putInt("RemainingAdditionalStock",
				rawSlotTag.getIntOr(ResourceGaugeHelper.REMAINING_ADDITIONAL_STOCK_KEY, 0));
		}
		return stateTag;
	}
}
