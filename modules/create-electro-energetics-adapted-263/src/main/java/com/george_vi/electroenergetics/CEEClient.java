package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.client.*;
import com.george_vi.electroenergetics.client.render.*;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntityRenderer;
import com.george_vi.electroenergetics.content.electric_fan.ElectricFanBlockEntityRenderer;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorRenderer;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelRenderer;
import com.george_vi.electroenergetics.content.electrical_panel.link.*;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorRenderer;
import com.george_vi.electroenergetics.content.frequency_meter.FrequencyMeterRenderer;
import com.george_vi.electroenergetics.content.fuse.FuseHolderRenderer;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeRenderer;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbRenderer;
import com.george_vi.electroenergetics.content.potentiometer.PotentiometerRenderer;
import com.george_vi.electroenergetics.content.potentiometer.RedstonePotentiometerRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographRenderer;
import com.george_vi.electroenergetics.content.railway_electrification.third_rail.RailContactShoeRenderer;
import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterRenderer;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesRenderer;
import com.george_vi.electroenergetics.content.synchroscope.SynchroscopeRenderer;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_switch.HVSwitchRenderer;
import com.george_vi.electroenergetics.content.variac.RedstoneVariacRenderer;
import com.george_vi.electroenergetics.content.variac.VariacRenderer;
import com.george_vi.electroenergetics.events.CEEClientEvents;
import com.george_vi.electroenergetics.ponder.CEEPonderPlugin;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllMenuScreens;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.content.kinetics.base.*;
import com.zurrtum.create.client.foundation.item.*;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.core.registries.BuiltInRegistries;

public class CEEClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CEEPartialModels.register();
        registerRenderers();
        CEEBlockEntityBehaviours.register();
        CEEMovementRenders.register();
        CEEPackets.registerClient();
        CEEClientEvents.register();
        ItemModels.ID_MAPPER.put(HandToolModel.ID, HandToolModel.Unbaked.CODEC);
        PonderIndex.addPlugin(new CEEPonderPlugin());
        AllMenuScreens.register(
                CEEMenuTypes.ELECTRICAL_PANEL_LINK_MENU.get(),
                (mc, type, id, inv, title, data) ->
                        new ElectricalPanelLinkScreen(
                                new ElectricalPanelLinkMenu(type, id, inv, data), inv, title));
        AllMenuScreens.register(
                CEEMenuTypes.BIDIRECTIONAL_LINK_MENU.get(),
                (mc, type, id, inv, title, data) ->
                        new BiDirectionalLinkScreen(
                                new BiDirectionalLinkMenu(type, id, inv, data), inv, title));
        registerFluids();
        registerItemStats();
        BuiltInRegistries.ITEM.forEach(
                item -> {
                    if (!BuiltInRegistries.ITEM
                            .getKey(item)
                            .getNamespace()
                            .equals(CreateElectroEnergetics.ID)) return;
                    TooltipModifier.REGISTRY.register(
                            item,
                            new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                                    .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
                                    .andThen(new ElectricStatsTooltipModifier(item)));
                });
    }

    private static void registerRenderers() {
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.VOLTMETER.get(), ElectricGaugeRenderer::voltmeter);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.AMMETER.get(), ElectricGaugeRenderer::ammeter);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.ELECTRIC_FAN.get(), ElectricFanBlockEntityRenderer::new);
        AllBlockEntityRenders.render(CEEBlockEntityTypes.BULB.get(), BulbBlockEntityRenderer::new);
        AllBlockEntityRenders.render(CEEBlockEntityTypes.HV_SWITCH.get(), HVSwitchRenderer::new);
        AllBlockEntityRenders.render(CEEBlockEntityTypes.PANTOGRAPH.get(), PantographRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.RAIL_CONTACT_SHOE.get(), RailContactShoeRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.CATENARY_HOLDER.get(), CatenaryHolderRenderer::new);
        AllBlockEntityRenders.render(CEEBlockEntityTypes.RESISTOR.get(), ResistorRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.FUSE_HOLDER.get(), FuseHolderRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.INDICATOR_BULB.get(), IndicatorBulbRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.POTENTIOMETER.get(), PotentiometerRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.REDSTONE_POTENTIOMETER.get(),
                RedstonePotentiometerRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.RESISTIVE_HEATER.get(), ResistiveHeaterRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.SYNCHROSCOPE.get(), SynchroscopeRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.FREQUENCY_METER.get(), FrequencyMeterRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.ELECTRICAL_PANEL.get(), ElectricalPanelRenderer::new);
        AllBlockEntityRenders.render(CEEBlockEntityTypes.VARIAC.get(), VariacRenderer::new);
        AllBlockEntityRenders.render(
                CEEBlockEntityTypes.REDSTONE_VARIAC.get(), RedstoneVariacRenderer::new);
        AllBlockEntityRenders.visual(
                CEEBlockEntityTypes.ELECTRIC_MOTOR.get(),
                ElectricMotorRenderer::new,
                OrientedRotatingVisual.of(CEEPartialModels.ELECTRIC_MOTOR_SHAFT));
        AllBlockEntityRenders.visual(
                CEEBlockEntityTypes.ALTERNATOR_ROTOR.get(),
                KineticBlockEntityRenderer::new,
                SingleAxisRotatingVisual.of(CEEPartialModels.ROTOR));
        AllBlockEntityRenders.visual(
                CEEBlockEntityTypes.ALTERNATOR_BRUSHES.get(),
                AlternatorBrushesRenderer::new,
                OrientedRotatingVisual.of(CEEPartialModels.ALTERNATOR_BRUSHES_SHAFT));
    }

    private static void registerFluids() {
        var transformer =
                new net.minecraft.client.renderer.block.FluidModel.Unbaked(
                        new net.minecraft.client.resources.model.sprite.Material(
                                CreateElectroEnergetics.rl("block/fluid/transformer_oil_still")),
                        new net.minecraft.client.resources.model.sprite.Material(
                                CreateElectroEnergetics.rl("block/fluid/transformer_oil_flow")),
                        null,
                        null);
        var plant =
                new net.minecraft.client.renderer.block.FluidModel.Unbaked(
                        new net.minecraft.client.resources.model.sprite.Material(
                                CreateElectroEnergetics.rl("block/fluid/plant_oil_still")),
                        new net.minecraft.client.resources.model.sprite.Material(
                                CreateElectroEnergetics.rl("block/fluid/plant_oil_flow")),
                        null,
                        null);
        com.zurrtum.create.client.AllFluidConfigs.MODEL.put(
                (net.minecraft.world.level.material.FlowingFluid)
                        CEEFluids.TRANSFORMER_OIL.getSource(),
                transformer);
        com.zurrtum.create.client.AllFluidConfigs.MODEL.put(
                (net.minecraft.world.level.material.FlowingFluid) CEEFluids.PLANT_OIL.getSource(),
                plant);
    }

    private static void registerItemStats() {
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEItems.CREATIVE_WIRE_SPOOL.get(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addResistancePerMeter(() -> 0.00001d));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.HV_CAPACITOR.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxVoltage(
                                CEEConfigs.server().voltageValues.highVoltageCapacitorVoltage
                                        ::get));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.BULB.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addResistance(CEEConfigs.server().resistanceValues.bulbResistance::get)
                        .addVoltage(() -> 300));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.AMMETER.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxCurrent(CEEConfigs.server().voltageValues.maxAmmeterCurrent::get));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.VOLTMETER.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxVoltage(CEEConfigs.server().voltageValues.maxVoltmeterVoltage::get));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.TRANSFORMER.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet().addMaxPower(() -> 25000));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.RESISTOR.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet().addMaxPower(() -> 1300));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.BUZZER.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addResistance(() -> 1000)
                        .addMaxVoltage(() -> 100));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.CAPACITOR.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxVoltage(CEEConfigs.server().voltageValues.capacitorVoltage::get));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.INDUCTOR.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxVoltage(CEEConfigs.server().voltageValues.inductorVoltage::get));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.POTENTIOMETER.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxPower(CEEConfigs.server().powerValues.potentiometerMaxPower::get));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.REDSTONE_POTENTIOMETER.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxPower(CEEConfigs.server().powerValues.potentiometerMaxPower::get));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.VARIAC.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxPower(CEEConfigs.server().powerValues.variacMaxPower::get));
        ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                CEEBlocks.REDSTONE_VARIAC.get().asItem(),
                new ElectricStatsTooltipModifier.ElectricStatSet()
                        .addMaxPower(CEEConfigs.server().powerValues.variacMaxPower::get));
        for (var motor : CEEBlocks.ELECTRIC_MOTORS)
            ElectricStatsTooltipModifier.ALL_ENTRIES.register(
                    motor.asItem(),
                    new ElectricStatsTooltipModifier.ElectricStatSet()
                            .addResistance(
                                    CEEConfigs.server().resistanceValues.motorResistance::get));
        for (var wire :
                java.util.List.of(
                        CEEWireTypes.STANDARD.get(),
                        CEEWireTypes.HEAVILY_INSULATED.get(),
                        CEEWireTypes.COPPER.get(),
                        CEEWireTypes.ELECTRUM.get(),
                        CEEWireTypes.IRON.get(),
                        CEEWireTypes.IRON_BUS.get(),
                        CEEWireTypes.IRON_RAIL.get())) {
            var stats =
                    new ElectricStatsTooltipModifier.ElectricStatSet()
                            .addResistancePerMeter(wire::getResistance)
                            .addMaxCurrent(() -> wire.getMaxTemperature() / 30 + 33.33);
            if (wire.insulated()) stats.addMaxVoltage(wire::maxInsulationVoltage);
            ElectricStatsTooltipModifier.ALL_ENTRIES.register(wire.getSpooledItem(), stats);
        }
    }
}
