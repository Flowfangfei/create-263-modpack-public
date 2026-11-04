package com.george_vi.electroenergetics.content.transmission_distribution.transformer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEFluids;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.george_vi.electroenergetics.foundation.scroll_value.CEEValueBehaviour;
import com.zurrtum.create.catnip.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransformerCoreBlockEntity extends SmartBlockEntity {
    public TransformerCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public CEEValueBehaviour turns;
    public double power;
    public double lastSentPower = -1;
    public double heatDissipationFactor = 0;
    public double primaryVoltage;
    public double secondaryVoltage;

    @Environment(EnvType.CLIENT)
    public ElectricHumSoundInstance soundInstance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        turns = new CEEValueBehaviour(CEELang.translate("transformer.turns").component(), this) {

        };
        turns.between(1, 100);
        turns.value = 10;
        turns.withCallback(i -> this.updateTurns());
        behaviours.add(turns);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide())
            if (Math.abs(lastSentPower - power) > 100) {
                lastSentPower = power;
                sendData();
            }

        if (!level.isClientSide())
            return;
        com.george_vi.electroenergetics.foundation.ClientExecution.run(() -> this::tickAudio);
    }

    @Override
    public void lazyTick() {
        Direction facing = getBlockState().getValue(TransformerCoreBlock.FACING);
        if (level.isClientSide() || facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return;
        lastSentPower = power;

        TransformerCoreDevice device = DevicesSavedData.load((ServerLevel) level).getDevice(worldPosition, TransformerCoreDevice.class);
        if (device == null) {
            sendData();
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        visited.add(worldPosition);
        heatDissipatorsDFS(visited, worldPosition);
        double dissipationFactor = 1;
        for (BlockPos pos : visited) {
            if (worldPosition.equals(pos) || worldPosition.relative(facing).equals(pos))
                continue;
            dissipationFactor += 1d / Math.sqrt(net.minecraft.world.phys.Vec3.atCenterOf(pos).distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition).relative(facing, 0.5)));
        }

        if (!level.getFluidState(worldPosition).is(CEEFluids.TRANSFORMER_OIL.get().getSource()))
            dissipationFactor *= 0.7;
        if (!level.getFluidState(worldPosition.relative(getBlockState().getValue(TransformerCoreBlock.FACING))).is(CEEFluids.TRANSFORMER_OIL.get().getSource()))
            dissipationFactor *= 0.7;
        dissipationFactor = dissipationFactor * 60000;

        device.heatDissipation = heatDissipationFactor = dissipationFactor;
        sendData();
    }

    private void heatDissipatorsDFS(Set<BlockPos> visited, BlockPos currentPos) {
        boolean waterlogged = level.getFluidState(currentPos).is(FluidTags.WATER) ||
                level.getFluidState(currentPos).is(CEEFluids.TRANSFORMER_OIL.get().getSource());

        for (Direction direction : Iterate.directions) {
            BlockPos nextPos = currentPos.relative(direction);
            if (nextPos.distSqr(worldPosition) > 16)
                continue;
            BlockState state = level.getBlockState(nextPos);
            if ((waterlogged && !level.getFluidState(nextPos).isEmpty()) ||
                    (CEEBlocks.TRANSFORMER_CORE.has(state) && direction == getBlockState().getValue(TransformerCoreBlock.FACING)) ||
                state.is(CEETags.TRANSFORMER_HEAT_DISSIPATORS))
                if (visited.add(nextPos))
                    heatDissipatorsDFS(visited, nextPos);
        }
    }

    @Environment(EnvType.CLIENT)
    protected void tickAudio() {
        Direction facing = getBlockState().getValue(TransformerCoreBlock.FACING);
        BlockPos otherPos = worldPosition.relative(facing);

        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            return;

        if (power > 100) {
            if (soundInstance == null || soundInstance.isStopped())
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
            else if (soundInstance != null) {
                soundInstance.setVolume((float) Mth.clamp(power / 800000, 0.02, 0.125));
                soundInstance.keepAlive();
            }
        } else if (soundInstance != null);

    }

    private void updateTurns() {
        if (!(level instanceof ServerLevel sl))
            return;
        Direction facing = getBlockState().getValue(TransformerCoreBlock.FACING);
        BlockPos otherPos = worldPosition.relative(facing);
        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            if (level.getBlockEntity(otherPos) instanceof TransformerCoreBlockEntity be) {
                TransformerCoreDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, TransformerCoreDevice.class);
                if (device != null)
                    device.ratio = (double) turns.value / be.turns.value;
            }
        } else {
            if (level.getBlockEntity(otherPos) instanceof TransformerCoreBlockEntity be) {
                be.updateTurns();
            }
        }

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putDouble("HeatDissipationFactor", heatDissipationFactor);
        if (clientPacket) {
            tag.putDouble("Power", power);
            tag.putDouble("PV", primaryVoltage);
            tag.putDouble("SV", secondaryVoltage);
        }

        NbtView.write(output, tag);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        heatDissipationFactor = tag.getDoubleOr("HeatDissipationFactor", 0d);
        if (clientPacket) {
            power = tag.getDoubleOr("Power", 0d);
            primaryVoltage = tag.getDoubleOr("PV", 0d);
            secondaryVoltage = tag.getDoubleOr("SV", 0d);
        }

    }

}
