package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.devices.device.DeviceFeatureType;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.simulation.WireType;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class CEERegistries {
    public static final Registry<WireType> WIRE_TYPE =
            FabricRegistryBuilder.<WireType>createDefaulted(
                            ResourceKey.createRegistryKey(CreateElectroEnergetics.rl("wire_type")),
                            CreateElectroEnergetics.rl("standard"))
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();
    public static final Registry<WireAttachmentType> WIRE_ATTACHMENT_TYPE =
            FabricRegistryBuilder.<WireAttachmentType>createDefaulted(
                            ResourceKey.createRegistryKey(
                                    CreateElectroEnergetics.rl("wire_attachment_type")),
                            CreateElectroEnergetics.rl("empty"))
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();
    public static final Registry<WireInteractionBehaviour> WIRE_INTERACTION_BEHAVIOUR =
            FabricRegistryBuilder.<WireInteractionBehaviour>createDefaulted(
                            ResourceKey.createRegistryKey(
                                    CreateElectroEnergetics.rl("wire_interaction_behaviour")),
                            CreateElectroEnergetics.rl("empty"))
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();
    public static final Registry<ElectricTrainSoundType> ELECTRIC_TRAIN_SOUND_TYPE =
            FabricRegistryBuilder.<ElectricTrainSoundType>createDefaulted(
                            ResourceKey.createRegistryKey(
                                    CreateElectroEnergetics.rl("electric_train_sound_type")),
                            CreateElectroEnergetics.rl("modern"))
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();
    public static final Registry<PantographType> PANTOGRAPH_TYPE =
            FabricRegistryBuilder.<PantographType>createDefaulted(
                            ResourceKey.createRegistryKey(
                                    CreateElectroEnergetics.rl("pantograph_type")),
                            CreateElectroEnergetics.rl("standard"))
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();
    public static final Registry<PanelAttachmentType> PANEL_ATTACHMENT_TYPE =
            FabricRegistryBuilder.<PanelAttachmentType>create(
                            ResourceKey.createRegistryKey(
                                    CreateElectroEnergetics.rl("panel_attachment_type")))
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();
    public static final Registry<SimulatedDeviceType<?>> SIMULATED_DEVICE_TYPE =
            FabricRegistryBuilder.<SimulatedDeviceType<?>>create(
                            ResourceKey.createRegistryKey(
                                    CreateElectroEnergetics.rl("simulated_device_type")))
                    .buildAndRegister();
    public static final Registry<DeviceFeatureType> SIMULATED_DEVICE_FEATURE_TYPE =
            FabricRegistryBuilder.<DeviceFeatureType>create(
                            ResourceKey.createRegistryKey(
                                    CreateElectroEnergetics.rl("simulated_device_feature_type")))
                    .buildAndRegister();

    public static void register() {}
}
