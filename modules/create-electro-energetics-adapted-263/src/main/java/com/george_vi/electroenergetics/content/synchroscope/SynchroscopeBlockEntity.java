package com.george_vi.electroenergetics.content.synchroscope;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SynchroscopeBlockEntity extends SmartBlockEntity {
    public float phaseOffset = 0;
    public float prevPhaseOffset = 0;
    public float dialSpeed = 0;
    public int tickLength = 0;
    public int counter = 0;
    public boolean validConnection;
    public LerpedFloat smoothPhase = LerpedFloat.angular();
    public int redstoneSignal;
    public int ccwRedstoneSignal;
    public int cwRedstoneSignal;

    public SynchroscopeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
            prevPhaseOffset = phaseOffset;
        smoothPhase.tickChaser();

        if (!level.isClientSide()) {
            float unwrappedPhase = (phaseOffset + 360) % 360 - 180;
            if (unwrappedPhase > 0)
                unwrappedPhase = 180 - unwrappedPhase;
            else if (unwrappedPhase < 0)
                unwrappedPhase = -180 - unwrappedPhase;
            int newRedstoneSignal = ((-Math.round(unwrappedPhase * (16.0f / 360.0f)) % 16) + 16) % 16;
            int newCwSignal = Mth.clamp(Mth.floor(unwrappedPhase * 15 / 180 + 0.9), 0, 15);
            int newCcwRightSignal = Mth.clamp(Mth.floor(-unwrappedPhase * 15 / 180 + 0.9), 0, 15);
            if (newRedstoneSignal != redstoneSignal || newCwSignal != ccwRedstoneSignal || newCcwRightSignal != cwRedstoneSignal) {
                redstoneSignal = newRedstoneSignal;
                ccwRedstoneSignal = newCwSignal;
                cwRedstoneSignal = newCcwRightSignal;
                setChanged();
            }
        }
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        if (clientPacket) {
            prevPhaseOffset = phaseOffset;
            tickLength = Math.max(1, counter);
            counter = 0;
        }
        phaseOffset = tag.getFloatOr("PhaseOffset", 0f);
        validConnection = tag.getBooleanOr("ValidConnection", false);

        smoothPhase.chase(phaseOffset, 0.2f, LerpedFloat.Chaser.EXP);

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putFloat("PhaseOffset", phaseOffset);
        tag.putBoolean("ValidConnection", validConnection);

        NbtView.write(output, tag);
    }

}
