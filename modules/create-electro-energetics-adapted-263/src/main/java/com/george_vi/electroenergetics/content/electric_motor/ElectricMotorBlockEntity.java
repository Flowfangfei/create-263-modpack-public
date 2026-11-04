package com.george_vi.electroenergetics.content.electric_motor;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.foundation.scroll_value.KineticUnlockableScrollValueBehaviour;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.george_vi.electroenergetics.foundation.lang.CreateLang;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class ElectricMotorBlockEntity extends GeneratingKineticBlockEntity {

    public float load = 0;
    public float prevMotorCapacity;
    public float prevMotorSpeed;

    public KineticUnlockableScrollValueBehaviour generatedSpeed;

    public RMSHolder averageVoltage;

    @Environment(EnvType.CLIENT)
    public ElectricHumSoundInstance soundInstance;

    @Environment(EnvType.CLIENT)
    public ElectricHumSoundInstance windSoundInstance;

    public LerpedFloat soundSpeed = LerpedFloat.linear();

    public int ticksSinceChange = 0;
    public int speedChangeThreshold;

    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        averageVoltage = new RMSHolder(3 * CEEConfigs.server().simulationConfig.microTicks.get());
        speedChangeThreshold = FabricLoader.getInstance().isModLoaded("simulated") ? 1 : 12;
    }

    @Override
    public void tick() {
        super.tick();
        float motorCapacity = calculateMotorCapacity();
        float motorSpeed = calculateMotorSpeed();
        ticksSinceChange++;

        if ((motorCapacity != prevMotorCapacity || motorSpeed != prevMotorSpeed) && (ticksSinceChange >= speedChangeThreshold)) {
            prevMotorSpeed = motorSpeed;
            prevMotorCapacity = motorCapacity;
            reActivateSource = true;
            ticksSinceChange = 0;
        }
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Load", load);
        tag.putFloat("PrevSpeed", prevMotorSpeed);
        tag.putFloat("PrevCapacity", prevMotorCapacity);
        averageVoltage.write(tag, "Voltage");

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        load = tag.getFloatOr("Load", 0f);
        prevMotorSpeed = tag.getFloatOr("PrevSpeed", 0f);
        prevMotorCapacity = tag.getFloatOr("PrevCapacity", 0f);
        averageVoltage.read(tag, "Voltage");

    }

    @Override
    public void updateGeneratedRotation() {
        super.updateGeneratedRotation();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        int max = 256;
        generatedSpeed = new KineticUnlockableScrollValueBehaviour(CreateLang.translateDirect("kinetics.creative_motor.rotation_speed"),
                this);
        generatedSpeed.between(-max, max);
        generatedSpeed.value = 32;
        behaviours.add(generatedSpeed);
    }

    @Environment(EnvType.CLIENT)
    public void tickAudio() {
        float speed = isOverStressed() ? 0 : this.speed;
        soundSpeed.chase(speed, Math.abs(soundSpeed.getValue()) > Math.abs(speed) || generatedSpeed.isUnlocked() ? 13 : 1, LerpedFloat.Chaser.LINEAR);
        soundSpeed.tickChaser();
        float speedNormalized = Math.abs(soundSpeed.getValue()) / 256f;
        if (Math.abs(speedNormalized) < 0.1f)
            return;

        if (soundInstance == null || soundInstance.isStopped()) {
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(soundInstance = new ElectricHumSoundInstance(CEESoundEvents.DC_TRAIN.get(), worldPosition));
        } else if (soundInstance != null && speedNormalized != 0) {
            soundInstance.keepAlive();
            soundInstance.setVolume(Math.min(0.5f, speedNormalized * 0.75f));
            soundInstance.setPitch(Mth.lerp(speedNormalized, 0.1f, 2));
        }

        if (windSoundInstance == null || windSoundInstance.isStopped()) {
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(windSoundInstance = new ElectricHumSoundInstance(CEESoundEvents.TRAIN_WIND_STATIC.get(), worldPosition));
        } else if (windSoundInstance != null && speedNormalized != 0) {
            windSoundInstance.keepAlive();
            windSoundInstance.setVolume(speedNormalized * 0.5f);
            windSoundInstance.setPitch(Mth.lerp(speedNormalized, 0.1f, 1.5f));
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(calculateMotorSpeed());
        float capacity = speed == 0 ? 0 : calculateMotorCapacity(averageVoltage.get()) / speed;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        return convertToDirection(calculateMotorSpeed(), getBlockState().getValue(ElectricMotorBlock.FACING));
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        if (maxStress == 0)
            load = 0;
        else
            load = currentStress / maxStress;
        sendData();
    }

    @Override
    public int getFlickerScore() {
        // Makes create not destroy the block when it's speed is repeatedly changed.
        return 0;
    }

    float calculateMotorSpeed() {
        double voltage = averageVoltage.getSigned();
        if (Math.abs(voltage) < CEEConfigs.server().voltageValues.motorMinVoltage.get()) {
            return 0;
        } if (generatedSpeed.isUnlocked()) {
            float motorCapacity = calculateMotorCapacity(voltage);
            float xRPM = generatedSpeed.getUnlockedStressRPMMultiplier();

            return Mth.clamp(motorCapacity / xRPM,
                    -AllConfigs.server().kinetics.maxRotationSpeed.get(),
                    AllConfigs.server().kinetics.maxRotationSpeed.get()) * Mth.sign(voltage);
        } else {
            return generatedSpeed.getValue();
        }
    }

    float calculateMotorCapacity() {
        return calculateMotorCapacity(averageVoltage.getSigned());
    }

    float calculateMotorCapacity(double voltage) {
        return (float) ((voltage * voltage) / CEEConfigs.server().resistanceValues.motorResistance.get())
                * CEEConfigs.server().rotorValues.rotorStressMultiplier.getF();
    }
}
