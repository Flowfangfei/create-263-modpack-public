package com.george_vi.electroenergetics;

import static com.george_vi.electroenergetics.CreateElectroEnergetics.REGISTRATE;
import static com.george_vi.electroenergetics.registry.Registrations.movementBehaviour;

import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockItem;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.george_vi.electroenergetics.content.bundled_wire.BundledWireTerminationBlock;
import com.george_vi.electroenergetics.content.bundled_wire.BundledWireType;
import com.george_vi.electroenergetics.content.buzzer.BuzzerBlock;
import com.george_vi.electroenergetics.content.connector.*;
import com.george_vi.electroenergetics.content.converter.ConverterBlock;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlock;
import com.george_vi.electroenergetics.content.cut_off_switch.CutOffSwitchBlock;
import com.george_vi.electroenergetics.content.cut_off_switch.EmergencyStopBlock;
import com.george_vi.electroenergetics.content.cut_off_switch.MomentarySwitchBlock;
import com.george_vi.electroenergetics.content.electric_fan.ElectricFanBlock;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlock;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlock;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlock;
import com.george_vi.electroenergetics.content.electronic_components.capacitor.CapacitorBlock;
import com.george_vi.electroenergetics.content.electronic_components.diode.DiodeBlock;
import com.george_vi.electroenergetics.content.electronic_components.inductor.InductorBlock;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorBlock;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlock;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterItem;
import com.george_vi.electroenergetics.content.energy_meter.TriPolarEnergyMeterBlock;
import com.george_vi.electroenergetics.content.frequency_meter.FrequencyMeterBlock;
import com.george_vi.electroenergetics.content.fuse.FuseBlock;
import com.george_vi.electroenergetics.content.fuse.FuseBlockItem;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlock;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlock;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeMovementBehaviour;
import com.george_vi.electroenergetics.content.ground_rod.GroundRodBlock;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbBlock;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbBlockItem;
import com.george_vi.electroenergetics.content.pole.ConcretePoleBlock;
import com.george_vi.electroenergetics.content.pole.PoleMountBlock;
import com.george_vi.electroenergetics.content.potentiometer.PotentiometerBlock;
import com.george_vi.electroenergetics.content.potentiometer.RedstonePotentiometerBlock;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlock;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlock;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographMovementBehaviour;
import com.george_vi.electroenergetics.content.railway_electrification.third_rail.RailContactShoeBlock;
import com.george_vi.electroenergetics.content.railway_electrification.third_rail.RailContactShoeMovementBehaviour;
import com.george_vi.electroenergetics.content.redstone_relay.RedstoneRelayBlock;
import com.george_vi.electroenergetics.content.relay.RelayBlock;
import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterBlock;
import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterBlockEntity;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesBlock;
import com.george_vi.electroenergetics.content.rotor.AlternatorRotorBlock;
import com.george_vi.electroenergetics.content.rotor.StatorBlock;
import com.george_vi.electroenergetics.content.rotor.ThreePhaseAlternatorBrushesBlock;
import com.george_vi.electroenergetics.content.sign.WarningSignBlock;
import com.george_vi.electroenergetics.content.synchroscope.SynchroscopeBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.current_transformer.CurrentTransformerBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_capacitor.HVCapacitorBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_switch.HVSwitchBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.sf6_breaker.SF6BreakerBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.RadiatorPanelBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerCoreBlock;
import com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator.VoltageRegulatorBlock;
import com.george_vi.electroenergetics.content.variac.RedstoneVariacBlock;
import com.george_vi.electroenergetics.content.variac.VariacBlock;
import com.george_vi.electroenergetics.registry.BlockEntry;
import com.george_vi.electroenergetics.registry.SharedProperties;
import com.zurrtum.create.api.boiler.BoilerHeater;

import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class CEEBlocks {
    public static final BlockEntry<ConnectorBlock> CONNECTOR =
            REGISTRATE
                    .block("connector", ConnectorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<InsulatorBlock> INSULATOR =
            REGISTRATE
                    .block("insulator", InsulatorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<ConcretePoleBlock> CONCRETE_POLE =
            REGISTRATE
                    .block("concrete_pole", ConcretePoleBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<SF6BreakerBlock> SF6_BREAKER =
            REGISTRATE
                    .block("sulfur_hexafluoride_breaker", SF6BreakerBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<PoleMountBlock> POLE_MOUNT =
            REGISTRATE
                    .block("pole_mount", PoleMountBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<DoubleConnectorBlock> DOUBLE_CONNECTOR =
            REGISTRATE
                    .block("double_connector", DoubleConnectorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<TripleConnectorBlock> TRIPLE_CONNECTOR =
            REGISTRATE
                    .block("triple_connector", TripleConnectorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<HVCapacitorBlock> HV_CAPACITOR =
            REGISTRATE
                    .block("high_voltage_capacitor", HVCapacitorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<QuadConnectorBlock> QUAD_CONNECTOR =
            REGISTRATE
                    .block("quad_connector", QuadConnectorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<GroundRodBlock> GROUND_ROD =
            REGISTRATE
                    .block("ground_rod", GroundRodBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_ORANGE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<CreativeBatteryBlock> CREATIVE_BATTERY =
            REGISTRATE
                    .block("creative_battery", CreativeBatteryBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_PURPLE).forceSolidOn())
                    .item()
                    .properties(p -> p.rarity(Rarity.EPIC))
                    .build()
                    .register();

    public static final BlockEntry<FuseBlock> FUSE =
            REGISTRATE
                    .block("fuse", FuseBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .item(FuseBlockItem::standard)
                    .build()
                    .register();

    public static final BlockEntry<FuseBlock> BROKEN_FUSE =
            REGISTRATE
                    .block("broken_fuse", FuseBlock::broken)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .item(FuseBlockItem::blown)
                    .build()
                    .register();

    public static final BlockEntry<BulbBlock> BULB =
            REGISTRATE
                    .block("bulb", BulbBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(
                            p ->
                                    p.mapColor(MapColor.TERRACOTTA_WHITE)
                                            .lightLevel(
                                                    state ->
                                                            ((BulbBlock) state.getBlock())
                                                                    .lightEmission(state)))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<BulbBlock> BROKEN_BULB =
            REGISTRATE
                    .block("broken_bulb", BulbBlock::broken)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE).lightLevel(state -> 0))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<CutOffSwitchBlock> CUT_OFF_SWITCH =
            REGISTRATE
                    .block("cut_off_switch", properties -> new CutOffSwitchBlock(properties, false))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<CutOffSwitchBlock> DOUBLE_SWITCH =
            REGISTRATE
                    .block("double_switch", properties -> new CutOffSwitchBlock(properties, true))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<RedstoneRelayBlock> REDSTONE_RELAY =
            REGISTRATE
                    .block("redstone_relay", RedstoneRelayBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<EnergyMeterBlock> ENERGY_METER =
            REGISTRATE
                    .block("energy_meter", EnergyMeterBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item(EnergyMeterItem::new)
                    .properties(p -> p.stacksTo(1))
                    .build()
                    .register();

    public static final BlockEntry<TriPolarEnergyMeterBlock> TRI_POLAR_ENERGY_METER =
            REGISTRATE
                    .block("tri_polar_energy_meter", TriPolarEnergyMeterBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item(EnergyMeterItem::new)
                    .properties(p -> p.stacksTo(1))
                    .build()
                    .register();

    public static final BlockEntry<HVSwitchBlock> HV_SWITCH =
            REGISTRATE
                    .block("high_voltage_switch", HVSwitchBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<AccumulatorBlock> ACCUMULATOR =
            REGISTRATE
                    .block("accumulator", AccumulatorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item(AccumulatorBlockItem::new)
                    .build()
                    .register();

    public static final BlockEntry<ElectricGaugeBlock> AMMETER =
            REGISTRATE
                    .block("ammeter", ElectricGaugeBlock::ammeter)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
                    .onRegister(movementBehaviour(new ElectricGaugeMovementBehaviour(false)))
                    .item()
                    .register();

    public static final BlockEntry<ElectricGaugeBlock> VOLTMETER =
            REGISTRATE
                    .block("voltmeter", ElectricGaugeBlock::voltmeter)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
                    .onRegister(movementBehaviour(new ElectricGaugeMovementBehaviour(true)))
                    .item()
                    .register();

    public static final BlockEntry<ElectricPumpBlock> ELECTRIC_PUMP =
            REGISTRATE
                    .block("electric_pump", ElectricPumpBlock::new)
                    .initialProperties(SharedProperties::copperMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<ElectricFanBlock> ELECTRIC_FAN =
            REGISTRATE
                    .block("electric_fan", ElectricFanBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<TransformerBlock> TRANSFORMER =
            REGISTRATE
                    .block("transformer", TransformerBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<TransformerCoreBlock> TRANSFORMER_CORE =
            REGISTRATE
                    .block("transformer_core", TransformerCoreBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<RadiatorPanelBlock> RADIATOR_PANEL =
            REGISTRATE
                    .block("radiator_panel", RadiatorPanelBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<StatorBlock> STATOR =
            REGISTRATE
                    .block("stator", StatorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<VoltageRegulatorBlock> VOLTAGE_REGULATOR =
            REGISTRATE
                    .block("voltage_regulator", VoltageRegulatorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<CurrentTransformerBlock> CURRENT_TRANSFORMER =
            REGISTRATE
                    .block("current_transformer", CurrentTransformerBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<ElectricalPanelBlock> ELECTRICAL_PANEL =
            REGISTRATE
                    .block("electrical_panel", ElectricalPanelBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<AlternatorRotorBlock> ALTERNATOR_ROTOR =
            REGISTRATE
                    .block("alternator_rotor", AlternatorRotorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_ORANGE))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<AlternatorBrushesBlock> ALTERNATOR_BRUSHES =
            REGISTRATE
                    .block("alternator_brushes", AlternatorBrushesBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<ThreePhaseAlternatorBrushesBlock>
            THREE_PHASE_ALTERNATOR_BRUSHES =
                    REGISTRATE
                            .block(
                                    "three_phase_alternator_brushes",
                                    ThreePhaseAlternatorBrushesBlock::new)
                            .initialProperties(SharedProperties::stone)
                            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                            .item()
                            .build()
                            .register();

    public static final BlockEntry<Block> MAGNET_BLOCK =
            REGISTRATE
                    .block("magnet", Block::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<ConverterBlock> CONVERTER =
            REGISTRATE
                    .block("converter", ConverterBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<CatenaryHolderBlock> CATENARY_HOLDER =
            REGISTRATE
                    .block("catenary_holder", CatenaryHolderBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<PantographBlock> PANTOGRAPH =
            REGISTRATE
                    .block("pantograph", PantographBlock::new)
                    .initialProperties(SharedProperties::netheriteMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .onRegister(movementBehaviour(new PantographMovementBehaviour()))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<RailContactShoeBlock> RAIL_CONTACT_SHOE =
            REGISTRATE
                    .block("rail_contact_shoe", RailContactShoeBlock::new)
                    .initialProperties(SharedProperties::netheriteMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .onRegister(movementBehaviour(new RailContactShoeMovementBehaviour()))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<DiodeBlock> DIODE =
            REGISTRATE
                    .block("diode", DiodeBlock::new)
                    .initialProperties(SharedProperties::netheriteMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<ResistorBlock> RESISTOR =
            REGISTRATE
                    .block("resistor", properties -> new ResistorBlock(properties, false))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<ResistorBlock> CREATIVE_RESISTOR =
            REGISTRATE
                    .block("creative_resistor", properties -> new ResistorBlock(properties, true))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_PURPLE))
                    .item()
                    .properties(p -> p.rarity(Rarity.EPIC))
                    .build()
                    .register();

    public static final BlockEntry<FuseHolderBlock> FUSE_HOLDER =
            REGISTRATE
                    .block("fuse_holder", FuseHolderBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<BuzzerBlock> BUZZER =
            REGISTRATE
                    .block("buzzer", BuzzerBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<RelayBlock> RELAY =
            REGISTRATE
                    .block("relay", RelayBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<CapacitorBlock> CAPACITOR =
            REGISTRATE
                    .block("capacitor", CapacitorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<InductorBlock> INDUCTOR =
            REGISTRATE
                    .block("inductor", InductorBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<MomentarySwitchBlock> MOMENTARY_SWITCH =
            REGISTRATE
                    .block("momentary_switch", MomentarySwitchBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<EmergencyStopBlock> EMERGENCY_STOP_BUTTON =
            REGISTRATE
                    .block("emergency_stop_button", EmergencyStopBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<IndicatorBulbBlock> INDICATOR_BULB =
            REGISTRATE
                    .block("indicator_bulb", IndicatorBulbBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))

                    // why are loot table gens so looong wtf
                    .item(IndicatorBulbBlockItem::new)
                    .build()
                    .register();

    public static final BlockEntry<WarningSignBlock> HIGH_VOLTAGE_SIGN =
            REGISTRATE
                    .block("high_voltage_sign", WarningSignBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<WarningSignBlock> ELECTRIC_SHOCK_SIGN =
            REGISTRATE
                    .block("electric_shock_sign", WarningSignBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<WarningSignBlock> GROUNDING_SIGN =
            REGISTRATE
                    .block("grounding_sign", WarningSignBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<PotentiometerBlock> POTENTIOMETER =
            REGISTRATE
                    .block("potentiometer", PotentiometerBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<RedstonePotentiometerBlock> REDSTONE_POTENTIOMETER =
            REGISTRATE
                    .block("redstone_potentiometer", RedstonePotentiometerBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<VariacBlock> VARIAC =
            REGISTRATE
                    .block("variac", VariacBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<RedstoneVariacBlock> REDSTONE_VARIAC =
            REGISTRATE
                    .block("redstone_variac", RedstoneVariacBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    @SuppressWarnings("unused")
    public static final BlockEntry<BundledWireTerminationBlock> DUPLEX_WIRE_TERMINATION =
            REGISTRATE
                    .block(
                            "duplex_wire_termination",
                            p -> new BundledWireTerminationBlock(p, BundledWireType.DUPLEX))
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<ResistiveHeaterBlock> RESISTIVE_HEATER =
            REGISTRATE
                    .block("resistive_heater", ResistiveHeaterBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .onRegister(
                            (b) ->
                                    BoilerHeater.REGISTRY.register(
                                            b,
                                            ((level, pos, state) -> {
                                                if (level.getBlockEntity(pos)
                                                        instanceof ResistiveHeaterBlockEntity be)
                                                    return state.getValue(ResistiveHeaterBlock.LIT)
                                                            ? Mth.clamp(be.heat, 0, 1.5f)
                                                            : -1;
                                                return -1;
                                            })))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<SynchroscopeBlock> SYNCHROSCOPE =
            REGISTRATE
                    .block("synchroscope", SynchroscopeBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<FrequencyMeterBlock> FREQUENCY_METER =
            REGISTRATE
                    .block("frequency_meter", FrequencyMeterBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                    .item()
                    .build()
                    .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntry<ElectricMotorBlock>[] ELECTRIC_MOTORS =
            new BlockEntry[DyeColor.values().length];

    @SuppressWarnings("unchecked")
    public static final BlockEntry<ElectricalPanelBlock>[] DYED_ELECTRICAL_PANELS =
            new BlockEntry[DyeColor.values().length];

    static {
        for (DyeColor color : DyeColor.values()) {
            ELECTRIC_MOTORS[color.ordinal()] =
                    REGISTRATE
                            .block(
                                    color.getSerializedName() + "_electric_motor",
                                    ElectricMotorBlock::new)
                            .initialProperties(SharedProperties::stone)
                            .properties(p -> p.mapColor(color))
                            .item()
                            .build()
                            .register();

            DYED_ELECTRICAL_PANELS[color.ordinal()] =
                    REGISTRATE
                            .block(
                                    color.getSerializedName() + "_electrical_panel",
                                    properties -> new ElectricalPanelBlock(properties, color))
                            .initialProperties(SharedProperties::stone)
                            .properties(p -> p.mapColor(color))
                            .item()
                            .build()
                            .register();
        }
    }

    public static void register() {}
}
