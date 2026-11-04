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
import net.createmod.ponder.api.client.PonderIndex;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.simibubi.create.Create.asResource;

@EventBusSubscriber(modid = CreateElectroEnergetics.ID, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CEEPonderPlugin());
        RadialWrenchMenu.registerRotationProperty(DoubleConnectorBlock.ROLL, "Roll");
        RadialWrenchMenu.registerRotationProperty(DoubleConnectorBlock.STYLE, "Style");
        RadialWrenchMenu.registerRotationProperty(ConnectorBlock.STYLE, "Style");
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, CreateElectroEnergetics.rl("electric_properties_overlay"), ElectricPropertiesOverlay.INSTANCE);
    }
}
