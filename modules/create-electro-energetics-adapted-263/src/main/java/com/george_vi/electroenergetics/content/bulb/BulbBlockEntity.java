package com.george_vi.electroenergetics.content.bulb;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BulbBlockEntity extends SmartBlockEntity {
    public float light = 0;
    public LerpedFloat smoothLight = LerpedFloat.linear();
    public BulbBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    @Override
    public void tick() {
        if (level.isClientSide())
            smoothLight.tickChaser();
    }

    public void setLight(float light) {
        this.light = light;
        smoothLight.chase(light, 0.5f, LerpedFloat.Chaser.LINEAR);
        if (!level.isClientSide())
            sendData();
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Light", light);

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        light = tag.getFloatOr("Light", 0f);
        if (clientPacket)
            smoothLight.chase(light, 0.5f, LerpedFloat.Chaser.EXP);

    }
}
