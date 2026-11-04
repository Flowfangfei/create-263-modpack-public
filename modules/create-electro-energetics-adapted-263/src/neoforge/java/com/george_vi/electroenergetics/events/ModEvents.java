package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.events.datagen.CEEAdvancements;
import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterRenderer;
import com.george_vi.electroenergetics.content.connector.ConnectorBlock;
import com.george_vi.electroenergetics.content.connector.DoubleConnectorBlock;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickRenderer;
import com.george_vi.electroenergetics.ponder.CEEPonderPlugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu;
import com.simibubi.create.content.equipment.wrench.WrenchItemRenderer;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.FilesHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.simibubi.create.Create.asResource;

@EventBusSubscriber(modid = CreateElectroEnergetics.ID)
public class ModEvents {



    public static final AtomicBoolean changedConfigs = new AtomicBoolean(false);

    @SubscribeEvent
    public static void reloadModConfig(ModConfigEvent.Reloading event) {
        changedConfigs.set(true);
    }










    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event) {
        event.register(CEERegistries.WIRE_TYPE);
        event.register(CEERegistries.WIRE_ATTACHMENT_TYPE);
        event.register(CEERegistries.WIRE_INTERACTION_BEHAVIOUR);
        event.register(CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE);
        event.register(CEERegistries.PANTOGRAPH_TYPE);
        event.register(CEERegistries.PANEL_ATTACHMENT_TYPE);
        event.register(CEERegistries.SIMULATED_DEVICE_FEATURE_TYPE);
        event.register(CEERegistries.SIMULATED_DEVICE_TYPE);
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
            CEEAdvancements.registerTrigger();
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    }
}

