package com.george_vi.electroenergetics.content.converter;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.google.common.collect.ImmutableList;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.api.lang.Lang;
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.TransferPreconditions;

import java.util.List;

public class ConverterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    ConverterDevice converterDevice = null;

    public ConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected ScrollValueBehaviour voltage;
    double power;
    double storedEnergy;

    ConverterEnergyStorage capability = new ConverterEnergyStorage(this);

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        voltage = new ScrollValueBehaviour(Component.translatable("electroenergetics.converter.voltage"),
                this, new ValueBox()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(
                        label, max, 10,
                        ImmutableList.of(Component.translatable("electroenergetics.converter.voltage_symbol")),
                        new ValueSettingsFormatter(valueSettings ->
                                CEELang.formatVoltage(valueSettings.value() * 10).component()));
            }
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

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                CEEBlockEntityTypes.CONVERTER.get(),
                (be, context) ->
                        context == null || be.getBlockState().getValue(ConverterBlock.FACING).getOpposite() == context ? be.capability : null);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (Math.abs(power) <= 1)
            return false;
        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .add(power < 0 ? CEELang.formatPower(Math.abs(power)) :
                        CEELang.formatFEPerTick(Math.abs(power / CEEConfigs.server().wattFeTConversionRate.get())))
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_load")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.energy_generation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .add(power > 0 ? CEELang.formatPower(Math.abs(power)) :
                        CEELang.formatFEPerTick(Math.abs(power / CEEConfigs.server().wattFeTConversionRate.get())))
                .style(ChatFormatting.AQUA)
                .space()
                .add(Component.translatable("electroenergetics.gui.goggles.at_current_load")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("Power", power);
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        power = tag.getDoubleOr("Power", 0d);
        storedEnergy = tag.getDoubleOr("StoredEnergy", 0d);
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 5);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis() == state.getValue(ConverterBlock.FACING).getAxis() &&
                    state.getValue(ConverterBlock.SOURCE);
        }
    }

    /** Shares the simulation buffer with the transactional NeoForge FE capability. */
    public static class ConverterEnergyStorage extends SnapshotJournal<Double> implements EnergyHandler {
        final ConverterBlockEntity be;

        public ConverterEnergyStorage(ConverterBlockEntity be) {
            this.be = be;
        }

        private ConverterDevice device() {
            if (!(be.getLevel() instanceof ServerLevel level)) return null;
            if (be.converterDevice == null || !be.converterDevice.isValid())
                be.converterDevice = DevicesSavedData.load(level).getDevice(be.getBlockPos(), ConverterDevice.class);
            return be.converterDevice;
        }

        private double conversionRate() {
            return CEEConfigs.server().wattFeTConversionRate.get();
        }

        @Override
        public long getAmountAsLong() {
            ConverterDevice device = device();
            double energy = device != null ? device.storedEnergy : be.storedEnergy;
            return Math.max(0L, (long) (energy / conversionRate()));
        }

        @Override
        public long getCapacityAsLong() {
            return Math.max(0L, (long) (CEEConfigs.server().converterMaxPowerKw.get() * 1000 / conversionRate()));
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            ConverterDevice device = device();
            if (device == null || device.isSource) return 0;
            int transferred = (int) Math.min(amount, Math.min(Integer.MAX_VALUE, getAmountAsLong()));
            if (transferred > 0) {
                updateSnapshots(transaction);
                device.storedEnergy -= transferred * conversionRate();
            }
            return transferred;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            ConverterDevice device = device();
            if (device == null || !device.isSource) return 0;
            double room = Math.max(0, CEEConfigs.server().converterMaxPowerKw.get() * 1000 - device.storedEnergy);
            int transferred = (int) Math.min(amount, Math.min(Integer.MAX_VALUE, room / conversionRate()));
            if (transferred > 0) {
                updateSnapshots(transaction);
                device.storedEnergy += transferred * conversionRate();
            }
            return transferred;
        }

        @Override
        protected Double createSnapshot() {
            return be.converterDevice.storedEnergy;
        }

        @Override
        protected void revertToSnapshot(Double snapshot) {
            be.converterDevice.storedEnergy = snapshot;
        }

        @Override
        protected void onRootCommit(Double originalState) {
            be.storedEnergy = be.converterDevice.storedEnergy;
            be.setChanged();
            be.sendData();
        }
    }
}
