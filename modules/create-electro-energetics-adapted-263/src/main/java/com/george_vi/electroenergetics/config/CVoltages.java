package com.george_vi.electroenergetics.config;


public class CVoltages extends CEEConfigBase {
    public final ConfigInt motorMinVoltage = i(80, 0, "motorMinVoltage", "[in Volts]");
    public final ConfigInt trainMinVoltage = i(1900, 0, "trainMinVoltage", "[in Volts]");
    public final ConfigInt trainMaxVoltage = i(5000, 0, "trainMaxVoltage", "[in Volts]");
    public final ConfigDouble wireMaxVoltage = d(1500, 0, "wireMaxVoltage", "[in Volts]");
    public final ConfigDouble heavilyInsulatedWireMaxVoltage = d(20_000, 0, "heavilyInsulatedWireMaxVoltage", "[in Volts]");
    public final ConfigDouble highVoltageCapacitorVoltage = d(25_000, 0, "highVoltageCapacitorVoltage", "[in Volts]");
    public final ConfigDouble capacitorVoltage = d(500, 0, "capacitorVoltage", "[in Volts]");
    public final ConfigDouble inductorVoltage = d(500, 0, "inductorVoltage", "[in Volts]");
    public final ConfigDouble maxVoltmeterVoltage = d(10_000, 0, "maxVoltmeterVoltage", "[in Volts]");
    public final ConfigDouble maxAmmeterCurrent = d(200, 0, "maxAmmeterCurrent", "[in Amps]");
    public final ConfigDouble maxAccumulatorCharge = d(360000, 0, "maxAccumulatorCharge", "[in Coulombs]");

    @Override
    public String getName() {
        return "Voltages";
    }

}
