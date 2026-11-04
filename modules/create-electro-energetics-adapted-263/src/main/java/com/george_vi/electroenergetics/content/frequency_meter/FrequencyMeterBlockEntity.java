package com.george_vi.electroenergetics.content.frequency_meter;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FrequencyMeterBlockEntity extends SmartBlockEntity {
    public float frequency = 0;
    public float prevFrequency = 0;
    public int tickLength = 0;
    public int counter = 0;
    public LerpedFloat smoothFrequency = LerpedFloat.angular();

    public FrequencyMeterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();
        counter = Mth.clamp(counter + 1, 0, 20);
        if (counter > 20)
            prevFrequency = frequency;
        if (AngleHelper.getShortestAngleDiff(smoothFrequency.getValue(), frequency) > 1)
            smoothFrequency.chase(frequency, 0.1f, LerpedFloat.Chaser.EXP);
        smoothFrequency.tickChaser();
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        if (clientPacket) {
            prevFrequency = frequency;
            tickLength = Math.max(1, counter);
            counter = 0;
        }
        frequency = tag.getFloatOr("Frequency", 0f);

        float diff = (AngleHelper.getShortestAngleDiff(frequency, prevFrequency) % 180) / 180f;
        if (Float.isNaN(smoothFrequency.getValue()))
            smoothFrequency.setValue(0);
        smoothFrequency.chase(frequency, Mth.lerp(diff, 0.1, 1), LerpedFloat.Chaser.EXP);

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Frequency", frequency);

        NbtView.write(output, tag);
    }

}
