package com.george_vi.electroenergetics.content.transmission_distribution.current_transformer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ScalingScrollValueBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CurrentTransformerBlockEntity extends SmartBlockEntity {
    public CurrentTransformerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public ScalingScrollValueBehaviour scaling;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        scaling = new ScalingScrollValueBehaviour(CEELang.translateDirect("gauge.scaling"), this);
        scaling.withCallback(i -> updateScale());
        behaviours.add(scaling);
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

    }

    private void updateScale() {
        if (!(level instanceof ServerLevel sl))
            return;

        CurrentTransformerDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, CurrentTransformerDevice.class);

        if (device != null) {
            device.ratio = scaling.getScale();
        }

        if (!getBlockState().getValue(CurrentTransformerBlock.BOTTOM))
            if (level.getBlockEntity(worldPosition.below()) instanceof CurrentTransformerBlockEntity be)
                be.scaling.setValue(scaling.getValue());
    }

}
