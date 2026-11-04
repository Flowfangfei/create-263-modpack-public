package com.george_vi.electroenergetics.content.rotor;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.config.CRotor;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDevice;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class AlternatorBrushesBlockEntity extends KineticBlockEntity {

    public List<AlternatorRotorBlockEntity> rotors = new ArrayList<>();
    public BlockPos otherBrush = null;
    public float voltage;
    public AlternatorBrushesBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (rotors.size() < 16)
            getRotors();
        for (AlternatorRotorBlockEntity rotor : rotors) {
            if (rotor.isRemoved()) {
                getRotors();
                break;
            }
        }
        float totalStress = 0;
        for (AlternatorRotorBlockEntity rotor : rotors) {
            totalStress += rotor.magnets * Math.abs(rotor.getSpeed());
        }
        CRotor rotorConfig = CEEConfigs.server().rotorValues;
        totalStress *= rotorConfig.rotorPowerMultiplier.getF();
        if (!(level instanceof ServerLevel sl))
            return;

        SimulatedDevice device = DevicesSavedData.load(sl).getDevice(worldPosition);

        if (!(device instanceof AlternatorBrushesDevice dataHolder)) {
            if (device instanceof ThreePhaseAlternatorBrushesDevice dataHolder) {
                dataHolder.stress = totalStress;
                dataHolder.voltage = totalStress / rotorConfig.rotorFullLoadCurrent.getF();
                dataHolder.otherBrush = otherBrush;
                float speed = getSpeed();
                dataHolder.rpmSpeed = Float.isFinite(dataHolder.rpmSpeed) ?
                        LerpedFloat.Chaser.LINEAR.chase(dataHolder.rpmSpeed, Math.abs(dataHolder.rpmSpeed) > Math.abs(speed) ? 5 : 2, speed) :
                        speed;
            }
            return;
        }

        dataHolder.stress = totalStress;
        dataHolder.voltage = totalStress / rotorConfig.rotorFullLoadCurrent.getF();
        dataHolder.otherBrush = otherBrush;
    }

    public void getRotors() {
        List<AlternatorRotorBlockEntity> rotors = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            BlockPos rotorPos = worldPosition.relative(getBlockState().getValue(AlternatorBrushesBlock.FACING).getOpposite(), i + 1);
            BlockEntity rawBE = level.getBlockEntity(rotorPos);
            if (rawBE instanceof AlternatorBrushesBlockEntity be && be.getBlockState().getValue(AlternatorBrushesBlock.FACING) == getBlockState().getValue(AlternatorBrushesBlock.FACING).getOpposite()) {
                otherBrush = rotorPos;
                break;
            }
            if (!(rawBE instanceof AlternatorRotorBlockEntity be && be.getAxis() == getBlockState().getValue(AlternatorBrushesBlock.FACING).getAxis())) {
                otherBrush = null;
                break;
            }
            rotors.add(be);
        }
        this.rotors = rotors;
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        if (clientPacket)
            tag.putFloat("V", voltage);

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        if (clientPacket)
            voltage = tag.getFloatOr("V", 0f);

    }
}
