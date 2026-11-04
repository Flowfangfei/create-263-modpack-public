package com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.VoltageScrollValueBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class VoltageRegulatorBlockEntity extends SmartBlockEntity {
    public VoltageRegulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public VoltageScrollValueBehaviour voltage;
    public double power;
    public double lastSentPower = -1;
    public double inputVoltage;
    public double outputVoltage;

    @Environment(EnvType.CLIENT)
    public ElectricHumSoundInstance soundInstance;

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide())
            if (Math.abs(lastSentPower - power) > 100) {
                lastSentPower = power;
                sendData();
            }

        if (!level.isClientSide())
            return;

        com.george_vi.electroenergetics.foundation.ClientExecution.run(() -> this::tickAudio);
    }

    @Override
    public void lazyTick() {
        if (level.isClientSide())
            return;
        lastSentPower = power;
        sendData();
    }

    @Environment(EnvType.CLIENT)
    protected void tickAudio() {
        if (power > 100) {
            if (soundInstance == null || soundInstance.isStopped())
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
            else if (soundInstance != null) {
                soundInstance.setVolume((float) Mth.clamp(power / 800000, 0.02, 0.25));
                soundInstance.keepAlive();
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

        voltage = new VoltageScrollValueBehaviour(CEELang.translate("voltage_regulator.voltage").component(), this);

        voltage.withCallback(i -> this.updateVoltage());
        behaviours.add(voltage);
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        if (clientPacket) {
            tag.putDouble("P", power);
            tag.putDouble("I", inputVoltage);
            tag.putDouble("O", outputVoltage);
        }

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        if (clientPacket) {
            power = tag.getDoubleOr("P", 0d);
            inputVoltage = tag.getDoubleOr("I", 0d);
            outputVoltage = tag.getDoubleOr("O", 0d);
        }

    }

    private void updateVoltage() {
        if (!(level instanceof ServerLevel sl))
            return;
        VoltageRegulatorDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, VoltageRegulatorDevice.class);

        if (device != null) {
            device.targetVoltage = voltage.getVoltage();
        }

        if (!getBlockState().getValue(VoltageRegulatorBlock.BOTTOM))
            if (level.getBlockEntity(worldPosition.below()) instanceof VoltageRegulatorBlockEntity be)
                be.voltage.setValue(voltage.value);
    }

}
