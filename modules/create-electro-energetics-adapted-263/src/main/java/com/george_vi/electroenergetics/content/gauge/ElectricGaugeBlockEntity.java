package com.george_vi.electroenergetics.content.gauge;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ScalingScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ElectricGaugeBlockEntity extends SmartBlockEntity {
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public final boolean voltmeter;
    public double voltage;
    public ScalingScrollValueBehaviour scaling;
    public int redstoneSignal;

    ElectricGaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean voltmeter) {
        super(type, pos, state);
        this.voltmeter = voltmeter;
    }

    public static ElectricGaugeBlockEntity voltmeter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new ElectricGaugeBlockEntity(type, pos, state, true);
    }

    public static ElectricGaugeBlockEntity ammeter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new ElectricGaugeBlockEntity(type, pos, state, false);
    }

    @Override
    public void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag compound = new CompoundTag();

        compound.putFloat("Value", dialTarget);
        compound.putDouble("Voltage", voltage);

        NbtView.write(output, compound);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag compound = NbtView.read(input);

        dialTarget = compound.getFloatOr("Value", 0f);
        voltage = compound.getDoubleOr("Voltage", 0d);

    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            prevDialState = dialState;
            dialState += (dialTarget - dialState) * .125f;
            if (dialState > 1 && level.getRandom().nextFloat() < 1 / 2f)
                dialState -= (dialState - 1) * level.getRandom().nextFloat();
        } else {
            int newRedstoneSignal = dialTarget < 0.001 ? 0 : Mth.ceil(Mth.clamp(dialTarget * 15, 0, 15));
            if (newRedstoneSignal != redstoneSignal) {
                redstoneSignal = newRedstoneSignal;
                setChanged();
            }
        }

    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        scaling = new ScalingScrollValueBehaviour(CEELang.translateDirect("gauge.scaling"), this);
        scaling.withCallback(i -> updateScale());
        behaviours.add(scaling);
    }

    private void updateScale() {

    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    public void setValue(double v) {
        dialTarget = (float) Mth.clamp(voltmeter ? v / 1000 : v / 40, 0, 1);
        sendData();
    }

}
