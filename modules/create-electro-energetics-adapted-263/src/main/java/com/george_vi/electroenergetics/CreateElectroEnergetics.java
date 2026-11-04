package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.compat.computercraft.CCProxy;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.CEEPanelAttachmentTypes;
import com.george_vi.electroenergetics.content.fuse.FuseHoldables;
import com.george_vi.electroenergetics.events.GameEvents;
import com.george_vi.electroenergetics.events.ModEvents;
import com.george_vi.electroenergetics.registry.Registrations;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;

public class CreateElectroEnergetics implements ModInitializer {
    public static final String ID = "electroenergetics";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrations REGISTRATE = new Registrations();

    @Override
    public void onInitialize() {
        CEEConfigs.register();
        CEEDataComponents.register();
        REGISTRATE.registerBlockItems();
        CEEItems.register();
        CEEFluids.registerItems();
        CEEWireTypes.register();
        CEEMobEffects.register();
        CEESoundEvents.register();
        CEEDisplaySources.register();
        CEEBlockEntityTypes.register();
        CEEMenuTypes.register();
        CEEEntityTypes.register();
        CEEPantographTypes.register();
        CEEWireAttachments.register();
        CEEPanelAttachmentTypes.register();
        CEEElectricTrainSoundTypes.register();
        CEEWireInteractionBehaviours.register();
        CEESimulatedDeviceFeatureTypes.register();
        CEESimulatedDevices.register();
        CEECreativeTab.register();
        CEEPackets.register();
        FuseHoldables.register();
        ConverterBlockEntity.registerEnergyStorage();
        CCProxy.register();
        ModEvents.register();
        GameEvents.register();
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
