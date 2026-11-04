package com.george_vi.electroenergetics.content.converter;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.EnergyStorage;

public class ConverterDevice extends SimpleElectricalDevice {
    public boolean isSource;
    public double voltage;
    public double resistance;
    public double storedEnergy;
    public ConverterBlockEntity be;

    public ConverterDevice(Level level, BlockPos pos, DevicesSavedData deviceSD, SimulatedDeviceType<?> type) {
        super(level, pos, deviceSD, type);
    }

    private double getMaxEnergy() {
        return CEEConfigs.server().converterMaxPowerKw.get() * 1000;
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        super.preTick(bridges);
        if (this.isSource && this.storedEnergy > 1) {
            bridges.builder(pos)
                    .energyLimitedSource(0, 1, this.storedEnergy, this.voltage);
        } else
            bridges.builder(pos)
                    .resistor(0, 1, this.resistance == 0 ? 999999 : this.resistance);
    }

    @Override
    public void postTick(SimulationResults results) {

        double displayedPower;
        double vd = Math.abs(results.getVoltageAt(pos, 0, 1));
        if (this.isSource) {

            double current = results.getCurrentThrough(pos, 0, 1);
            double power = Math.abs(current) * vd;

            this.storedEnergy -= power;

            if (this.storedEnergy < getMaxEnergy() && level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                if (!CEEBlocks.CONVERTER.has(state))
                    return;
                EnergyStorage energyStorage = EnergyStorage.SIDED.find(level,
                        pos.relative(state.getValue(ConverterBlock.FACING).getOpposite()),
                        state.getValue(ConverterBlock.FACING));
                if (energyStorage != null) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long received = energyStorage.extract(Math.max(0, (long) ((getMaxEnergy() - this.storedEnergy) / CEEConfigs.server().wattFeTConversionRate.get())), transaction);
                        transaction.commit();
                        this.storedEnergy += received * CEEConfigs.server().wattFeTConversionRate.get();
                    }
                }
            }

            displayedPower = power;
        } else {

            double power = Math.abs(vd * results.getCurrentThrough(pos, 0, 1));

            this.storedEnergy += power;

            if (this.storedEnergy > 0 && level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                if (!CEEBlocks.CONVERTER.has(state))
                    return;
                EnergyStorage energyStorage = EnergyStorage.SIDED.find(level,
                        pos.relative(state.getValue(ConverterBlock.FACING).getOpposite()),
                        state.getValue(ConverterBlock.FACING));
                if (energyStorage != null) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long sent = energyStorage.insert(Math.max(0, (long) (this.storedEnergy / CEEConfigs.server().wattFeTConversionRate.get())), transaction);
                        transaction.commit();
                        this.storedEnergy -= sent * CEEConfigs.server().wattFeTConversionRate.get();
                    }
                }
            }

            displayedPower = -power;
            if (vd > 1)
                this.resistance = Math.max(CEEConfigs.server().resistanceValues.converterMinResistance.get(), vd / (Math.max((getMaxEnergy() - this.storedEnergy), 0.01) / vd));

        }

        this.storedEnergy = Mth.clamp(this.storedEnergy, 0, getMaxEnergy());

        if (this.be == null && level.isLoaded(pos))
            if (level.getBlockEntity(pos) instanceof ConverterBlockEntity be)
                this.be = be;

        if (this.be != null) {
            if (this.be.isRemoved())
                this.be = null;
            else {
                if (Math.abs(this.be.power - displayedPower) > 1 ||
                        Math.abs(this.be.storedEnergy - this.storedEnergy) > 10) {
                    this.be.power = displayedPower;
                    this.be.storedEnergy = this.storedEnergy;
                    this.be.sendData();
                }
            }
        }
    }

    @Override
    public void read(CompoundTag tag) {
        this.voltage = tag.getDoubleOr("Voltage", 0d);
        this.resistance = tag.getDoubleOr("Resistance", 0d);
        this.storedEnergy = tag.getDoubleOr("StoredEnergy", 0d);
        this.isSource = tag.getBooleanOr("Source", false);
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putBoolean("Source", this.isSource);
        tag.putDouble("Voltage", this.voltage);
        tag.putDouble("Resistance", this.resistance);
        tag.putDouble("StoredEnergy", this.storedEnergy);
    }
}
