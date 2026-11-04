package com.george_vi.electroenergetics.content.electric_pump;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.fluids.pump.PumpBlockEntity;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricPumpBlockEntity extends PumpBlockEntity {
    public float voltage = 0;

    public ElectricPumpBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public boolean isValidBlockState(BlockState state) {
        return com.george_vi.electroenergetics.CEEBlocks.ELECTRIC_PUMP.has(state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return com.george_vi.electroenergetics.CEEBlockEntityTypes.ELECTRIC_PUMP.get();
    }

    @Override
    public net.minecraft.core.Holder<BlockEntityType<?>> typeHolder() {
        return getType().builtInRegistryHolder();
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Voltage", voltage);

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        voltage = tag.getFloatOr("Voltage", 0f);

    }

    @Override
    public float getSpeed() {
        return (float) Mth.clamp(voltage * voltage / CEEConfigs.server().resistanceValues.pumpResistance.get() / AllConfigs.server().kinetics.stressValues.getImpact(AllBlocks.MECHANICAL_PUMP).getAsDouble(), -300, 300);
    }

    public void setVoltage(float voltage) {
        if (Math.abs(voltage - this.voltage) < 1)
            return;
        if (Math.abs(voltage) <= 1)
            voltage = 0;
        this.voltage = voltage;
        if (Math.abs(voltage) > 0.5)
            award(AllAdvancements.PUMP);
        if (level.isClientSide() && !isVirtual())
            return;

        updatePressureChange();
    }

    public double getResistance() {
        return CEEConfigs.server().resistanceValues.pumpResistance.get();
    }
}
