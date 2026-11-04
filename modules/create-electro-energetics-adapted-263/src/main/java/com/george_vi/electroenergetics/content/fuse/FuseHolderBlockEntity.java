package com.george_vi.electroenergetics.content.fuse;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHoldable;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FuseHolderBlockEntity extends SmartBlockEntity {
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        if (level != null && !level.isClientSide()) {
            if (firstFuse != null) net.minecraft.world.Containers.dropContents(level, pos, firstFuse.getFirst().getDrops(firstFuse.getSecond()));
            if (secondFuse != null) net.minecraft.world.Containers.dropContents(level, pos, secondFuse.getFirst().getDrops(secondFuse.getSecond()));
        }
        super.preRemoveSideEffects(pos, oldState);
    }

    public Pair<FuseHoldable, CompoundTag> firstFuse;
    public Pair<FuseHoldable, CompoundTag> secondFuse;

    public FuseHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        if (firstFuse != null) {
            tag.putString("FirstID", firstFuse.getFirst().getID().toString());
            tag.put("FirstData", firstFuse.getSecond());
        }
        if (secondFuse != null) {
            tag.putString("SecondID", secondFuse.getFirst().getID().toString());
            tag.put("SecondData", secondFuse.getSecond());
        }

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        Identifier firstID = Identifier.tryParse(tag.getStringOr("FirstID", ""));
        if (firstID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(firstID);
            if (type != null) {
                firstFuse = Pair.of(type, tag.getCompoundOrEmpty("FirstData"));
            } else
                firstFuse = null;
        } else
            firstFuse = null;

        Identifier secondID = Identifier.tryParse(tag.getStringOr("SecondID", ""));
        if (secondID != null) {
            FuseHoldable type = FuseHoldable.ALL.get(secondID);
            if (type != null) {
                secondFuse = Pair.of(type, tag.getCompoundOrEmpty("SecondData"));
            } else
                secondFuse = null;
        } else
            secondFuse = null;

    }

    public void updateFuses() {
        sendData();
        if (level instanceof ServerLevel sl) {
            FuseHolderDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, FuseHolderDevice.class);
            if (device == null)
                return;

            if (firstFuse != null) {
                device.firstFuse = firstFuse.getFirst();
                device.firstData = firstFuse.getSecond();
            } else {
                device.firstFuse = null;
                device.firstData = null;
            }

            if (secondFuse != null) {
                device.secondFuse = secondFuse.getFirst();
                device.secondData = secondFuse.getSecond();
            } else {
                device.secondFuse = null;
                device.secondData = null;
            }
        }
    }

    public void updateFusesFromDevice(CompoundTag firstData, CompoundTag secondData) {
        boolean update = false;
        if (firstFuse != null && firstData != null) {
            if (firstData.getBooleanOr("UpdateThisTick", false)) {
                firstData.remove("UpdateThisTick");
                update = true;
            }
            firstFuse = Pair.of(firstFuse.getFirst(), firstData);
        }
        if (secondFuse != null && secondData != null) {
            if (secondData.getBooleanOr("UpdateThisTick", false)) {
                secondData.remove("UpdateThisTick");
                update = true;
            }
            secondFuse = Pair.of(secondFuse.getFirst(), secondData);
        }

        if (update)
            sendData();
    }
}
