package com.yision.fluidlogistics.content.logistics.factoryGauge;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.utility.CreateLang;
import com.yision.fluidlogistics.migration.factoryGauge.LegacyFactoryGaugeMigration;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

@ApiStatus.Internal
public final class ResourceFactoryGaugeNbt {
	public static final String TYPE_KEY = "CFLGaugeType";
	public static final String STATE_KEY = "CFLResourceGauge";
	public static final String ENHANCEMENTS_VISIBLE_KEY = "EnhancementsVisible";

	private ResourceFactoryGaugeNbt() {
		throw new AssertionError("This class should not be instantiated");
	}

	public static String slotTagName(PanelSlot slot) {
		return CreateLang.asId(slot.name());
	}

	@Nullable
	public static Identifier determineTypeId(CompoundTag rawSlotTag, HolderLookup.Provider registries) {
		String typeIdString = rawSlotTag.getStringOr(TYPE_KEY,"");
		if (!typeIdString.isEmpty()) {
			Identifier typeId = Identifier.tryParse(typeIdString);
			if (typeId != null)
				return typeId;
		}

		return LegacyFactoryGaugeMigration.determineTypeId(rawSlotTag, registries);
	}

	public static void writeTypeAndSettings(CompoundTag panelTag, Identifier typeId,
		int restockThreshold, int promiseLimit, int additionalStock, boolean enhancementsVisible) {
		panelTag.putString(TYPE_KEY, typeId.toString());
		CompoundTag stateTag = new CompoundTag();
		stateTag.putInt("RestockThreshold", restockThreshold);
		stateTag.putInt("PromiseLimit", promiseLimit);
		stateTag.putInt("AdditionalStock", additionalStock);
		stateTag.putBoolean(ENHANCEMENTS_VISIBLE_KEY, enhancementsVisible);
		panelTag.put(STATE_KEY, stateTag);
	}

	public static void writeRuntimeState(CompoundTag panelTag, ResourceFactoryGaugeRuntime runtime) {
		CompoundTag stateTag = panelTag.getCompoundOrEmpty(STATE_KEY);
		runtime.write(stateTag);
		panelTag.put(STATE_KEY, stateTag);
	}
}
