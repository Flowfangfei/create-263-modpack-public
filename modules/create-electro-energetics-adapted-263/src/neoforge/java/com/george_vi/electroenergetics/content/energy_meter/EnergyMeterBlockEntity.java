package com.george_vi.electroenergetics.content.energy_meter;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ScalingScrollValueBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;
import java.util.UUID;

public class EnergyMeterBlockEntity extends SmartBlockEntity {

    public EnergyMeterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public double activePower = 0;
    public double totalEnergy = 0;
    public LerpedFloat smoothTotalEnergy = LerpedFloat.linear();
    public int ticks = 0;
    public boolean disconnected;
    public UUID owner;

    public ScalingScrollValueBehaviour scale;

    public void setTotalEnergy(double newTotalEnergy) {
        double d = (Math.abs(newTotalEnergy - totalEnergy));
        totalEnergy = newTotalEnergy;

        if ((d > 2 || ticks > 5) && !level.isClientSide()) {
            sendData();
            setChanged();
            ticks = 0;
        }
        ticks++;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide())
            smoothTotalEnergy.tickChaser();
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        boolean first = totalEnergy == -1;
        totalEnergy = tag.getDoubleOr("TotalEnergy", 0d);
        activePower = tag.getDoubleOr("ActivePower", 0d);
        disconnected = tag.getBooleanOr("Disconnected", false);
        setOwner(tag.contains("Owner") ? tag.read("Owner", net.minecraft.core.UUIDUtil.CODEC).orElse(null) : null);

        if (clientPacket)
            smoothTotalEnergy.chase(totalEnergy, first ? 1 : 0.5, LerpedFloat.Chaser.EXP);
    }

    public void setOwner(UUID uuid) {
        owner = uuid;
        scale.setPlayerPredicate(u -> u.equals(owner));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putDouble("TotalEnergy", totalEnergy);
        tag.putDouble("ActivePower", activePower);
        tag.putBoolean("Disconnected", disconnected);
        if (owner != null)
            tag.store("Owner", net.minecraft.core.UUIDUtil.CODEC, owner);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(scale = new ScalingScrollValueBehaviour(CEELang.translateDirect("gauge.scaling"),
                this, new ValueBox()));
    }

    static class ValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return super.getLocalOffset(level, pos, state);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(EnergyMeterBlock.FACING);
            return direction.getOpposite() == facing;
        }
    }


}
