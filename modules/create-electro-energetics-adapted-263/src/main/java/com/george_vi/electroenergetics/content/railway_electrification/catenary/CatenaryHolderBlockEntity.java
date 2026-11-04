package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.content.pole.ConcretePoleBlock;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CatenaryHolderBlockEntity extends SmartBlockEntity {

    static final Iterable<BlockPos.MutableBlockPos> offsets = BlockPos.spiralAround(BlockPos.ZERO, 4, Direction.EAST, Direction.SOUTH);
    public BlockPos attachedTo;

    public int poleWidth = 0;

    public CatenaryHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    @Override
    public void tick() {
        if (attachedTo == null || net.minecraft.world.phys.Vec3.atCenterOf(attachedTo).length() > 7 || !countsAsPole(attachedTo.offset(worldPosition))) {
            BlockPos polePos = null;
            float shortestDistance = 999;
            for (BlockPos.MutableBlockPos offset : offsets) {
                BlockPos pos = offset.immutable();
                float distance = (float) net.minecraft.world.phys.Vec3.atCenterOf(pos).length();
                if (distance < shortestDistance && countsAsPole(pos.offset(worldPosition))) {
                    polePos = pos;
                    shortestDistance = distance;
                }
            }
            if (polePos != null) {
                if (!polePos.equals(attachedTo))
                    invalidateRenderBoundingBox();
                attachedTo = polePos;
            } else
                attachedTo = null;
        }
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (attachedTo == null)
            return super.createRenderBoundingBox();
        return AABB.encapsulatingFullBlocks(worldPosition.below(), worldPosition.offset(attachedTo.getX(), 1, attachedTo.getZ()));
    }

    boolean countsAsPole(BlockPos pos) {
        poleWidth = getPoleWidth(pos);
        return poleWidth > 0;
    }

    int getPoleWidth(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FenceBlock)
            return 4;
        if (state.getBlock() instanceof GirderBlock || state.getBlock() instanceof WallBlock || state.getBlock() instanceof ConcretePoleBlock)
            return 8;
        return 0;
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        attachedTo = tag.read("AttachedTo", BlockPos.CODEC).orElse(null);

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        if (attachedTo != null)
            tag.store("AttachedTo", BlockPos.CODEC, attachedTo);

        NbtView.write(output, tag);
    }

    public BlockPos getAttachedTo() {
        return attachedTo;
    }

    public void removeAttachedTo() {
        attachedTo = null;
    }

    public void setAttachedTo(BlockPos pos) {
        attachedTo = pos;
    }
}
