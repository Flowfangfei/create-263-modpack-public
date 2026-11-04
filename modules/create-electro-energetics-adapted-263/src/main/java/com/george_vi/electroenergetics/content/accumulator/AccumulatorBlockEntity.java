package com.george_vi.electroenergetics.content.accumulator;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class AccumulatorBlockEntity extends SmartBlockEntity {
    public double cell1Charge = 0;
    public double cell2Charge = 0;
    public boolean isDoubleCell = false;

    public AccumulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(8);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        sendData();
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putDouble("Cell1Charge", cell1Charge);
        tag.putDouble("Cell2Charge", cell2Charge);
        if (isDoubleCell)
            tag.putBoolean("IsDoubleCell", true);

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        cell1Charge = tag.getDoubleOr("Cell1Charge", 0d);
        cell2Charge = tag.getDoubleOr("Cell2Charge", 0d);
        isDoubleCell = tag.getBooleanOr("IsDoubleCell", false);

    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

}
