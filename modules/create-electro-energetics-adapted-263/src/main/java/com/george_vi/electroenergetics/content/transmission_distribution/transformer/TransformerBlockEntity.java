package com.george_vi.electroenergetics.content.transmission_distribution.transformer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.george_vi.electroenergetics.foundation.scroll_value.CEEValueBehaviour;
import com.zurrtum.create.catnip.data.Couple;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class TransformerBlockEntity extends SmartBlockEntity {

    public TransformerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public CEEValueBehaviour ratio;
    public double power;
    public double lastSentPower = -1;
    public double primaryVoltage;
    public double secondaryVoltage;

    @Environment(EnvType.CLIENT)
    public ElectricHumSoundInstance soundInstance;

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
        if (level.isClientSide())
            return;
        lastSentPower = power;
        sendData();
    }

    @Environment(EnvType.CLIENT)
    protected void tickAudio() {

        if (power > 100) {
            if (soundInstance == null || soundInstance.isStopped())
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = new ElectricHumSoundInstance(worldPosition));
            else if (soundInstance != null) {
                soundInstance.setVolume((float) Mth.clamp(power / 800000, 0.02, 0.25));
                soundInstance.keepAlive();
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

        ratio = new CEEValueBehaviour(CEELang.translate("transformer.ratio").component(),
                this) {

        };
        ratio.between(0, ratios.length * 2);
        ratio.value = ratios.length;
        ratio.withFormatter(TransformerBlockEntity::indexToRatioString);
        ratio.withCallback(i -> this.updateRatio());
        behaviours.add(ratio);
    }

    static String indexToRatioString(int i) {
        if (i == ratios.length)
            return "1 / 1";
        if (i > ratios.length) {
            i -= ratios.length + 1;
            return ratios[i].getFirst() + " / " + ratios[i].getSecond();
        } else {
            i = ratios.length - i - 1;
            return ratios[i].getSecond() + " / " + ratios[i].getFirst();
        }
    }

    static double indexToRatio(int i) {
        if (i == ratios.length)
            return 1;
        if (i > ratios.length) {
            i -= ratios.length + 1;
            return (double) ratios[i].getFirst() / ratios[i].getSecond();
        } else {
            i = ratios.length - i - 1;
            return (double) ratios[i].getSecond() / ratios[i].getFirst();
        }
    }

    private void updateRatio() {
        if (!(level instanceof ServerLevel sl))
            return;
        TransformerDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, TransformerDevice.class);

        if (device != null)
            device.ratio = indexToRatio(ratio.value);
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

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

        if (clientPacket) {
            power = tag.getDoubleOr("Power", 0d);
            primaryVoltage = tag.getDoubleOr("PV", 0d);
            secondaryVoltage = tag.getDoubleOr("SV", 0d);
        }

    }

    @SuppressWarnings("unchecked")
    static Couple<Integer>[] ratios = new Couple[]{
            Couple.create(9, 10),
            Couple.create(8, 9),
            Couple.create(7, 8),
            Couple.create(6, 7),
            Couple.create(5, 6),
            Couple.create(4, 5),
            Couple.create(7, 9),
            Couple.create(3, 4),
            Couple.create(5, 7),
            Couple.create(2, 3),
            Couple.create(3, 5),
            Couple.create(4, 7),
            Couple.create(1, 2),
            Couple.create(3, 7),
            Couple.create(2, 5),
            Couple.create(3, 8),
            Couple.create(1, 3),
            Couple.create(2, 7),
            Couple.create(1, 4),
            Couple.create(2, 9),
            Couple.create(1, 5),
            Couple.create(2, 11),
            Couple.create(1, 6),
            Couple.create(1, 7),
            Couple.create(1, 8),
            Couple.create(1, 9),
            Couple.create(1, 10),
            Couple.create(1, 12),
            Couple.create(1, 14),
            Couple.create(1, 16),
            Couple.create(1, 18),
            Couple.create(1, 20),
            Couple.create(1, 22),
            Couple.create(1, 25),
            Couple.create(1, 28),
            Couple.create(1, 30),
            Couple.create(1, 33),
            Couple.create(1, 36),
            Couple.create(1, 40),
            Couple.create(1, 43),
            Couple.create(1, 46),
            Couple.create(1, 50)
    };

}
