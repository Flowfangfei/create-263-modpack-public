package com.george_vi.electroenergetics;

import static com.george_vi.electroenergetics.CreateElectroEnergetics.REGISTRATE;

import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.content.bulb.BulbBlockEntity;
import com.george_vi.electroenergetics.content.buzzer.BuzzerBlockEntity;
import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.creative_battery.CreativeBatteryBlockEntity;
import com.george_vi.electroenergetics.content.electric_fan.ElectricFanBlockEntity;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlockEntity;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electronic_components.capacitor.CapacitorBlockEntity;
import com.george_vi.electroenergetics.content.electronic_components.inductor.InductorBlockEntity;
import com.george_vi.electroenergetics.content.electronic_components.resistor.ResistorBlockEntity;
import com.george_vi.electroenergetics.content.energy_meter.EnergyMeterBlockEntity;
import com.george_vi.electroenergetics.content.frequency_meter.FrequencyMeterBlockEntity;
import com.george_vi.electroenergetics.content.fuse.FuseBlockEntity;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlockEntity;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.content.indicator_bulb.IndicatorBulbBlockEntity;
import com.george_vi.electroenergetics.content.potentiometer.PotentiometerBlockEntity;
import com.george_vi.electroenergetics.content.potentiometer.RedstonePotentiometerBlockEntity;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.CatenaryHolderBlockEntity;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlockEntity;
import com.george_vi.electroenergetics.content.railway_electrification.third_rail.RailContactShoeBlockEntity;
import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterBlockEntity;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesBlockEntity;
import com.george_vi.electroenergetics.content.rotor.AlternatorRotorBlockEntity;
import com.george_vi.electroenergetics.content.synchroscope.SynchroscopeBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.current_transformer.CurrentTransformerBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_capacitor.HVCapacitorBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.hv_switch.HVSwitchBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerCoreBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator.VoltageRegulatorBlockEntity;
import com.george_vi.electroenergetics.content.variac.RedstoneVariacBlockEntity;
import com.george_vi.electroenergetics.content.variac.VariacBlockEntity;
import com.george_vi.electroenergetics.registry.BlockEntityEntry;

public class CEEBlockEntityTypes {
    public static final BlockEntityEntry<EnergyMeterBlockEntity> ENERGY_METER =
            REGISTRATE
                    .blockEntity("energy_meter", EnergyMeterBlockEntity::new)
                    .displaySource(CEEDisplaySources.ENERGY_METER)
                    .displaySource(CEEDisplaySources.WATTMETER)
                    .validBlocks(CEEBlocks.ENERGY_METER::get, CEEBlocks.TRI_POLAR_ENERGY_METER::get)
                    .register();

    public static final BlockEntityEntry<AccumulatorBlockEntity> ACCUMULATOR =
            REGISTRATE
                    .blockEntity("accumulator", AccumulatorBlockEntity::new)
                    .validBlock(CEEBlocks.ACCUMULATOR)
                    .register();

    public static final BlockEntityEntry<ElectricGaugeBlockEntity> VOLTMETER =
            REGISTRATE
                    .blockEntity("voltmeter", ElectricGaugeBlockEntity::voltmeter)
                    .displaySource(CEEDisplaySources.VOLTAGE)
                    .validBlock(CEEBlocks.VOLTMETER::get)
                    .register();

    public static final BlockEntityEntry<ElectricGaugeBlockEntity> AMMETER =
            REGISTRATE
                    .blockEntity("ammeter", ElectricGaugeBlockEntity::ammeter)
                    .displaySource(CEEDisplaySources.AMPERAGE)
                    .validBlock(CEEBlocks.AMMETER::get)
                    .register();

    public static final BlockEntityEntry<ElectricFanBlockEntity> ELECTRIC_FAN =
            REGISTRATE
                    .blockEntity("electric_fan", ElectricFanBlockEntity::new)
                    .validBlock(CEEBlocks.ELECTRIC_FAN::get)
                    .register();

    public static final BlockEntityEntry<BulbBlockEntity> BULB =
            REGISTRATE
                    .blockEntity("bulb", BulbBlockEntity::new)
                    .validBlocks(CEEBlocks.BULB::get, CEEBlocks.BROKEN_BULB::get)
                    .register();

    public static final BlockEntityEntry<FuseBlockEntity> FUSE =
            REGISTRATE
                    .blockEntity("fuse", FuseBlockEntity::new)
                    .validBlocks(CEEBlocks.FUSE::get, CEEBlocks.BROKEN_FUSE::get)
                    .register();

    public static final BlockEntityEntry<CreativeBatteryBlockEntity> CREATIVE_BATTERY =
            REGISTRATE
                    .blockEntity("creative_battery", CreativeBatteryBlockEntity::new)
                    .validBlock(CEEBlocks.CREATIVE_BATTERY::get)
                    .register();

    public static final BlockEntityEntry<TransformerBlockEntity> TRANSFORMER =
            REGISTRATE
                    .blockEntity("transformer", TransformerBlockEntity::new)
                    .validBlocks(CEEBlocks.TRANSFORMER::get)
                    .register();

    public static final BlockEntityEntry<TransformerCoreBlockEntity> TRANSFORMER_CORE =
            REGISTRATE
                    .blockEntity("transformer_core", TransformerCoreBlockEntity::new)
                    .validBlocks(CEEBlocks.TRANSFORMER_CORE::get)
                    .register();

    public static final BlockEntityEntry<ElectricPumpBlockEntity> ELECTRIC_PUMP =
            REGISTRATE
                    .blockEntity("electric_pump", ElectricPumpBlockEntity::new)
                    .validBlocks(CEEBlocks.ELECTRIC_PUMP::get)
                    .register();

    public static final BlockEntityEntry<VoltageRegulatorBlockEntity> VOLTAGE_REGULATOR =
            REGISTRATE
                    .blockEntity("voltage_regulator", VoltageRegulatorBlockEntity::new)
                    .validBlock(CEEBlocks.VOLTAGE_REGULATOR::get)
                    .register();

    public static final BlockEntityEntry<ElectricMotorBlockEntity> ELECTRIC_MOTOR =
            REGISTRATE
                    .blockEntity("electric_motor", ElectricMotorBlockEntity::new)
                    .validBlocks(CEEBlocks.ELECTRIC_MOTORS)
                    .register();

    public static final BlockEntityEntry<AlternatorRotorBlockEntity> ALTERNATOR_ROTOR =
            REGISTRATE
                    .blockEntity("alternator_rotor", AlternatorRotorBlockEntity::new)
                    .validBlock(CEEBlocks.ALTERNATOR_ROTOR::get)
                    .register();

    public static final BlockEntityEntry<AlternatorBrushesBlockEntity> ALTERNATOR_BRUSHES =
            REGISTRATE
                    .blockEntity("alternator_brushes", AlternatorBrushesBlockEntity::new)
                    .validBlocks(
                            CEEBlocks.ALTERNATOR_BRUSHES, CEEBlocks.THREE_PHASE_ALTERNATOR_BRUSHES)
                    .register();

    public static final BlockEntityEntry<ConverterBlockEntity> CONVERTER =
            REGISTRATE
                    .blockEntity("converter", ConverterBlockEntity::new)
                    .validBlock(CEEBlocks.CONVERTER::get)
                    .register();

    public static final BlockEntityEntry<HVSwitchBlockEntity> HV_SWITCH =
            REGISTRATE
                    .blockEntity("high_voltage_switch", HVSwitchBlockEntity::new)
                    .validBlock(CEEBlocks.HV_SWITCH::get)
                    .register();

    public static final BlockEntityEntry<PantographBlockEntity> PANTOGRAPH =
            REGISTRATE
                    .blockEntity("pantograph", PantographBlockEntity::new)
                    .validBlock(CEEBlocks.PANTOGRAPH::get)
                    .register();

    public static final BlockEntityEntry<RailContactShoeBlockEntity> RAIL_CONTACT_SHOE =
            REGISTRATE
                    .blockEntity("rail_contact_shoe", RailContactShoeBlockEntity::new)
                    .validBlock(CEEBlocks.RAIL_CONTACT_SHOE::get)
                    .register();

    public static final BlockEntityEntry<CatenaryHolderBlockEntity> CATENARY_HOLDER =
            REGISTRATE
                    .blockEntity("catenary_holder", CatenaryHolderBlockEntity::new)
                    .validBlock(CEEBlocks.CATENARY_HOLDER::get)
                    .register();

    public static final BlockEntityEntry<ResistorBlockEntity> RESISTOR =
            REGISTRATE
                    .blockEntity("resistor", ResistorBlockEntity::new)
                    .validBlocks(CEEBlocks.RESISTOR::get, CEEBlocks.CREATIVE_RESISTOR::get)
                    .register();

    public static final BlockEntityEntry<FuseHolderBlockEntity> FUSE_HOLDER =
            REGISTRATE
                    .blockEntity("fuse_holder", FuseHolderBlockEntity::new)
                    .validBlock(CEEBlocks.FUSE_HOLDER::get)
                    .register();

    public static final BlockEntityEntry<BuzzerBlockEntity> BUZZER =
            REGISTRATE
                    .blockEntity("buzzer", BuzzerBlockEntity::new)
                    .validBlock(CEEBlocks.BUZZER::get)
                    .register();

    public static final BlockEntityEntry<CapacitorBlockEntity> CAPACITOR =
            REGISTRATE
                    .blockEntity("capacitor", CapacitorBlockEntity::new)
                    .validBlock(CEEBlocks.CAPACITOR::get)
                    .register();

    public static final BlockEntityEntry<InductorBlockEntity> INDUCTOR =
            REGISTRATE
                    .blockEntity("inductor", InductorBlockEntity::new)
                    .validBlock(CEEBlocks.INDUCTOR::get)
                    .register();

    public static final BlockEntityEntry<HVCapacitorBlockEntity> HV_CAPACITOR =
            REGISTRATE
                    .blockEntity("high_voltage_capacitor", HVCapacitorBlockEntity::new)
                    .validBlock(CEEBlocks.HV_CAPACITOR::get)
                    .register();

    public static final BlockEntityEntry<IndicatorBulbBlockEntity> INDICATOR_BULB =
            REGISTRATE
                    .blockEntity("indicator_bulb", IndicatorBulbBlockEntity::new)
                    .validBlock(CEEBlocks.INDICATOR_BULB::get)
                    .register();

    public static final BlockEntityEntry<PotentiometerBlockEntity> POTENTIOMETER =
            REGISTRATE
                    .blockEntity("potentiometer", PotentiometerBlockEntity::new)
                    .validBlock(CEEBlocks.POTENTIOMETER::get)
                    .register();

    public static final BlockEntityEntry<RedstonePotentiometerBlockEntity> REDSTONE_POTENTIOMETER =
            REGISTRATE
                    .blockEntity("redstone_potentiometer", RedstonePotentiometerBlockEntity::new)
                    .validBlock(CEEBlocks.REDSTONE_POTENTIOMETER::get)
                    .register();

    public static final BlockEntityEntry<ResistiveHeaterBlockEntity> RESISTIVE_HEATER =
            REGISTRATE
                    .blockEntity("resistive_heater", ResistiveHeaterBlockEntity::new)
                    .validBlock(CEEBlocks.RESISTIVE_HEATER::get)
                    .register();

    public static final BlockEntityEntry<SynchroscopeBlockEntity> SYNCHROSCOPE =
            REGISTRATE
                    .blockEntity("synchroscope", SynchroscopeBlockEntity::new)
                    .validBlock(CEEBlocks.SYNCHROSCOPE::get)
                    .register();

    public static final BlockEntityEntry<FrequencyMeterBlockEntity> FREQUENCY_METER =
            REGISTRATE
                    .blockEntity("frequency_meter", FrequencyMeterBlockEntity::new)
                    .validBlock(CEEBlocks.FREQUENCY_METER::get)
                    .register();

    public static final BlockEntityEntry<CurrentTransformerBlockEntity> CURRENT_TRANSFORMER =
            REGISTRATE
                    .blockEntity("current_transformer", CurrentTransformerBlockEntity::new)
                    .validBlock(CEEBlocks.CURRENT_TRANSFORMER::get)
                    .register();

    public static final BlockEntityEntry<ElectricalPanelBlockEntity> ELECTRICAL_PANEL =
            REGISTRATE
                    .blockEntity("electrical_panel", ElectricalPanelBlockEntity::new)
                    .validBlock(CEEBlocks.ELECTRICAL_PANEL)
                    .validBlocks(CEEBlocks.DYED_ELECTRICAL_PANELS)
                    .register();

    public static final BlockEntityEntry<VariacBlockEntity> VARIAC =
            REGISTRATE
                    .blockEntity("variac", VariacBlockEntity::new)
                    .validBlock(CEEBlocks.VARIAC)
                    .register();

    public static final BlockEntityEntry<RedstoneVariacBlockEntity> REDSTONE_VARIAC =
            REGISTRATE
                    .blockEntity("redstone_variac", RedstoneVariacBlockEntity::new)
                    .validBlock(CEEBlocks.REDSTONE_VARIAC)
                    .register();

    public static void register() {}
}
