package com.george_vi.electroenergetics.content.potentiometer;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ResistanceScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class RedstonePotentiometerBlockEntity extends SmartBlockEntity {

    public ResistanceScrollValueBehaviour resistance;
    public float progress;
    public LerpedFloat smoothProgress = LerpedFloat.linear();
    public float targetProgress;

    public RedstonePotentiometerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        resistance = new ResistanceScrollValueBehaviour(CEELang.translate("resistor.resistance").component(), this);
        resistance.withCallback(i -> this.updateResistance());
        behaviours.add(resistance);
    }

    @Override
    public void tick() {
        if (level instanceof ServerLevel sl) {
            float prevProgress = progress;
            progress = Mth.lerp(0.6f, progress, targetProgress);

            if (prevProgress != progress) {
                PotentiometerDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, PotentiometerDevice.class);

                if (device != null)
                    device.progress = progress;
                sendData();
            }
        } else {
            smoothProgress.tickChaser();
        }
    }

    private void updateResistance() {
        if (!(level instanceof ServerLevel sl))
            return;

        PotentiometerDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, PotentiometerDevice.class);

        if (device != null)
            device.resistance = resistance.getResistance();
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        progress = tag.getFloatOr("Progress", 0f);
        targetProgress = tag.getFloatOr("Target", 0f);
        if (clientPacket)
            smoothProgress.chase(progress, 0.1, LerpedFloat.Chaser.LINEAR);

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Progress", progress);
        tag.putFloat("Target", targetProgress);

        NbtView.write(output, tag);
    }

}
