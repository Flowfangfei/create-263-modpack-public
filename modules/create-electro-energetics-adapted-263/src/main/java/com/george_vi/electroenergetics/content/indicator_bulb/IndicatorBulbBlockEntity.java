package com.george_vi.electroenergetics.content.indicator_bulb;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class IndicatorBulbBlockEntity extends SmartBlockEntity {
    public DyeColor firstColor = DyeColor.WHITE;
    public DyeColor secondColor = DyeColor.WHITE;
    public float firstLight = 0f;
    public float secondLight = 0f;

    public LerpedFloat firstSmoothLight = LerpedFloat.linear();
    public LerpedFloat secondSmoothLight = LerpedFloat.linear();

    public IndicatorBulbBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        firstSmoothLight.tickChaser();
        secondSmoothLight.tickChaser();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        firstColor = DyeColor.byName(tag.getStringOr("FirstColor", ""), DyeColor.WHITE);
        secondColor = DyeColor.byName(tag.getStringOr("SecondColor", ""), DyeColor.WHITE);
        firstLight = tag.getFloatOr("FirstLight", 0f);
        secondLight = tag.getFloatOr("SecondLight", 0f);
        if (clientPacket) {
            firstSmoothLight.chase(firstLight, 0.75, LerpedFloat.Chaser.EXP);
            secondSmoothLight.chase(secondLight, 0.75, LerpedFloat.Chaser.EXP);
        }

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putString("FirstColor", firstColor.getSerializedName());
        tag.putString("SecondColor", secondColor.getSerializedName());
        tag.putFloat("FirstLight", firstLight);
        tag.putFloat("SecondLight", secondLight);

        NbtView.write(output, tag);
    }
}
