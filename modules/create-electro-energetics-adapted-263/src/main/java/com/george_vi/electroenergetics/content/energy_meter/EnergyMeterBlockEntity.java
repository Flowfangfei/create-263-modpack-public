package com.george_vi.electroenergetics.content.energy_meter;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ScalingScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

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
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putDouble("TotalEnergy", totalEnergy);
        tag.putDouble("ActivePower", activePower);
        tag.putBoolean("Disconnected", disconnected);
        if (owner != null)
            tag.store("Owner", net.minecraft.core.UUIDUtil.CODEC, owner);

        NbtView.write(output, tag);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(scale = new ScalingScrollValueBehaviour(CEELang.translateDirect("gauge.scaling"),
                this));
    }

}
