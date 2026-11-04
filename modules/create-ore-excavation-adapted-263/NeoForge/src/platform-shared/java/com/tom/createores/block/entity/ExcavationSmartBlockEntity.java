package com.tom.createores.block.entity;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Retains Create263's behaviour persistence while using native Value IO for excavation data. */
public abstract class ExcavationSmartBlockEntity extends SmartBlockEntity {
    protected ExcavationSmartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Override protected final void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        read(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag), clientPacket);
    }
    @Override protected final void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        write(output, clientPacket);
        tag.merge(output.buildResult());
    }
    protected void read(ValueInput input, boolean clientPacket) {}
    protected void write(ValueOutput output, boolean clientPacket) {}
}
