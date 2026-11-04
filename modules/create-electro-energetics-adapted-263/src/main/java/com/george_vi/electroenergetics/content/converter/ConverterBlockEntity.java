package com.george_vi.electroenergetics.content.converter;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.george_vi.electroenergetics.foundation.scroll_value.CEEValueBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import team.reborn.energy.api.EnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class ConverterBlockEntity extends SmartBlockEntity {
    public ConverterDevice converterDevice = null;

    public ConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public CEEValueBehaviour voltage;
    public double power;
    public double storedEnergy;

    public ConverterEnergyStorage capability = new ConverterEnergyStorage(this);

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        voltage = new CEEValueBehaviour(Component.translatable("electroenergetics.converter.voltage"),
                this) {

        };
        voltage.between(0, 432);
        voltage.value = 10;
        voltage.withFormatter(v -> CEELang.formatVoltage(v * 10).string());
        voltage.withCallback(i -> this.updateVoltage());
        behaviours.add(voltage);
    }

    private void updateVoltage() {
        if (!(level instanceof ServerLevel sl))
            return;

        if (converterDevice == null || !converterDevice.isValid()) {
            converterDevice = DevicesSavedData.load(sl).getDevice(getBlockPos(), ConverterDevice.class);
        }

        if (converterDevice != null) {
            converterDevice.voltage = voltage.value * 10;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel sl))
            return;

        if (converterDevice == null || !converterDevice.isValid()) {
            converterDevice = DevicesSavedData.load(sl).getDevice(getBlockPos(), ConverterDevice.class);
        }
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putDouble("Power", power);
        tag.putDouble("StoredEnergy", storedEnergy);

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        power = tag.getDoubleOr("Power", 0d);
        storedEnergy = tag.getDoubleOr("StoredEnergy", 0d);

    }

    public static void registerEnergyStorage() {
        EnergyStorage.SIDED.registerForBlockEntity((be, side) ->
                side == null || side == be.getBlockState().getValue(ConverterBlock.FACING).getOpposite()
                        ? be.capability : null, CEEBlockEntityTypes.CONVERTER.get());
    }

    public static class ConverterEnergyStorage extends SnapshotParticipant<Double> implements EnergyStorage {
        private final ConverterBlockEntity be;

        public ConverterEnergyStorage(ConverterBlockEntity be) { this.be = be; }

        private double conversion() { return CEEConfigs.server().wattFeTConversionRate.get(); }
        private ConverterDevice device() {
            if (be.converterDevice == null && be.getLevel() instanceof ServerLevel serverLevel)
                be.converterDevice = DevicesSavedData.load(serverLevel).getDevice(be.getBlockPos(), ConverterDevice.class);
            return be.converterDevice;
        }

        @Override
        public long getAmount() {
            ConverterDevice device = device();
            return Math.max(0, (long) Math.floor((device == null ? be.storedEnergy : device.storedEnergy) / conversion()));
        }

        @Override
        public long getCapacity() {
            return Math.max(0, (long) Math.floor(CEEConfigs.server().converterMaxPowerKw.get() * 1000 / conversion()));
        }

        @Override
        public boolean supportsInsertion() { return be.getBlockState().getValue(ConverterBlock.SOURCE); }

        @Override
        public boolean supportsExtraction() { return !supportsInsertion(); }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            if (maxAmount < 0) throw new IllegalArgumentException("Negative energy insertion");
            ConverterDevice device = device();
            if (device == null || !supportsInsertion()) return 0;
            double available = CEEConfigs.server().converterMaxPowerKw.get() * 1000 - device.storedEnergy;
            long inserted = Math.min(maxAmount, Math.max(0, (long) Math.floor(available / conversion())));
            if (inserted > 0) {
                updateSnapshots(transaction);
                device.storedEnergy += inserted * conversion();
            }
            return inserted;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            if (maxAmount < 0) throw new IllegalArgumentException("Negative energy extraction");
            ConverterDevice device = device();
            if (device == null || !supportsExtraction()) return 0;
            long extracted = Math.min(maxAmount, getAmount());
            if (extracted > 0) {
                updateSnapshots(transaction);
                device.storedEnergy -= extracted * conversion();
            }
            return extracted;
        }

        @Override
        protected Double createSnapshot() { return device() == null ? 0d : device().storedEnergy; }

        @Override
        protected void readSnapshot(Double snapshot) {
            if (device() != null) device().storedEnergy = snapshot;
        }

        @Override
        protected void onFinalCommit() { be.setChanged(); be.sendData(); }
    }
}
