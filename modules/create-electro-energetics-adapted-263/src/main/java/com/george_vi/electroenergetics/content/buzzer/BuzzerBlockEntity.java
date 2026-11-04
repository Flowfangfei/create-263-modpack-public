package com.george_vi.electroenergetics.content.buzzer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class BuzzerBlockEntity extends SmartBlockEntity {
    public double voltage = 0;

    @Environment(EnvType.CLIENT)
    public ElectricHumSoundInstance soundInstance;

    public BuzzerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        com.george_vi.electroenergetics.foundation.ClientExecution.run(() -> this::tickAudio);
    }

    @Environment(EnvType.CLIENT)
    void tickAudio() {
        if (Math.abs(voltage) < 0.1) {
            if (soundInstance != null) {
                soundInstance.setVolumeImmediately(0);
                soundInstance = null;
            }
            return;
        }
        if (soundInstance == null || soundInstance.isStopped()) {
            Minecraft.getInstance().getSoundManager()
                    .play(soundInstance = new ElectricHumSoundInstance(CEESoundEvents.BUZZER.get(), worldPosition));
        }
        soundInstance.setPitch(1f);
        soundInstance.keepAlive();
        soundInstance.setVolumeImmediately((float) Mth.clamp(Mth.lerp(voltage / 100f, -0.04f, 1.4f), 0f, 1.4f));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putDouble("Voltage", voltage);

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        voltage = tag.getDoubleOr("Voltage", 0d);

    }

    public void setVoltage(double voltage) {
        if (Math.abs(this.voltage - voltage) < 0.1)
            return;
        this.voltage = voltage;
        sendData();
    }
}
