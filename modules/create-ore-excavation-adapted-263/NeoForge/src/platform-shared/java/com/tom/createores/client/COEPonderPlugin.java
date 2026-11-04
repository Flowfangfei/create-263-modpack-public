package com.tom.createores.client;

import net.createmod.ponder.api.client.registration.PonderPlugin;
import net.createmod.ponder.api.client.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.client.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.Identifier;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tom.createores.CreateOreExcavation;
import com.tom.createores.Registration;

public class COEPonderPlugin implements PonderPlugin {
	public static final Identifier DRILLING = Identifier.fromNamespaceAndPath(CreateOreExcavation.MODID, "drilling");

	@Override
	public String getModId() {
		return CreateOreExcavation.MODID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
		PonderSceneRegistrationHelper<DeferredHolder<?, ?>> HELPER = helper.withKeyFunction(DeferredHolder::getId);

		HELPER.forComponents(Registration.DRILL_BLOCK, Registration.EXTRACTOR_BLOCK)
		.addStoryBoard("drilling_machine", PonderScenes::drillingMachine, AllCreatePonderTags.KINETIC_SOURCES, DRILLING);

		HELPER.forComponents(Registration.VEIN_FINDER_ITEM, Registration.SAMPLE_DRILL_BLOCK, Registration.VEIN_ATLAS_ITEM)
		.addStoryBoard("vein_finder", PonderScenes::oreFinder, AllCreatePonderTags.KINETIC_SOURCES, DRILLING)
		.addStoryBoard("sample_drill", PonderScenes::sampleDrill, DRILLING);
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<Identifier> helper) {
		helper.registerTag(DRILLING)
		.addToIndex()
		.item(Registration.DIAMOND_DRILL_ITEM.get(), true, false)
		.title("Drilling")
		.description("Extract various resources from underground")
		.register();

		PonderTagRegistrationHelper<DeferredHolder<?, ?>> HELPER = helper.withKeyFunction(DeferredHolder::getId);

		HELPER.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES).
		add(Registration.DRILL_BLOCK).
		add(Registration.EXTRACTOR_BLOCK);
	}
}
