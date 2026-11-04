package com.george_vi.electroenergetics.content.variac;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.george_vi.electroenergetics.foundation.scroll_value.CEEValueBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class VariacBlockEntity extends KineticBlockEntity {

    public CEEValueBehaviour ratio;
    public float progress;
    public LerpedFloat smoothProgress = LerpedFloat.linear();

    public VariacBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        ratio = new CEEValueBehaviour(CEELang.translate("variac.ratio").component(),
                this) {

        };

        ratio.between(0, ratios.length);
        ratio.value = ratios.length / 2 - 1;
        ratio.withFormatter(i -> indexToRatio(i) + CEELang.string("variac.ratio_scaling_symbol"));
        ratio.withCallback(i -> this.updateRatio());
        behaviours.add(ratio);
    }

    public static double indexToRatio(int value) {
        return ratios[Mth.clamp(value, 0, ratios.length - 1)];
    }

    @Override
    public void tick() {
        if (level instanceof ServerLevel sl) {
            float prevProgress = progress;
            progress = Mth.clamp(progress + getSpeed() / 5000, 0, 1);
            if (prevProgress != progress) {
                VariacDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, VariacDevice.class);

                if (device != null)
                    device.progress = progress;
                sendData();
            }
        } else {
            smoothProgress.tickChaser();
        }
    }

    private void updateRatio() {
        if (!(level instanceof ServerLevel sl))
            return;

        VariacDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, VariacDevice.class);

        if (device != null)
            device.ratio = indexToRatio(ratio.value);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        progress = tag.getFloatOr("Progress", 0f);
        if (clientPacket)
            smoothProgress.chase(progress, 0.1, LerpedFloat.Chaser.LINEAR);

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Progress", progress);

        NbtView.write(output, tag);
    }

    @Override
    public int getFlickerScore() {
        // Makes create not destroy the block when it's speed is repeatedly changed.
        return 0;
    }

    static double[] ratios = new double[]{
            0.1d,
            0.2d,
            0.3d,
            0.4d,
            0.5d,
            0.6d,
            0.7d,
            0.8d,
            0.9d,
            1.0d,
            1.1d,
            1.2d,
            1.3d,
            1.4d,
            1.5d,
            1.6d,
            1.7d,
            1.8d,
            1.9d,
            2.0d,
    };
}
