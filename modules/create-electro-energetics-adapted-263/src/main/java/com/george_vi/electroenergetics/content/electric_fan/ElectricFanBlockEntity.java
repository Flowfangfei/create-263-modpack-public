package com.george_vi.electroenergetics.content.electric_fan;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.george_vi.electroenergetics.foundation.NbtView;
import com.george_vi.electroenergetics.CEESoundEvents;
import com.george_vi.electroenergetics.content.ElectricHumSoundInstance;
import com.zurrtum.create.content.kinetics.fan.AirCurrent;
import com.zurrtum.create.content.kinetics.fan.IAirCurrentSource;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElectricFanBlockEntity extends SmartBlockEntity implements IAirCurrentSource {
    public float actualSpeed;
    public float targetSpeed;
    public float rotation = 0;
    public float prevRotation = 0;

    public AirCurrent airCurrent;
    public int airCurrentUpdateCooldown;
    public int entitySearchCooldown;
    public boolean updateAirFlow;

    @Environment(EnvType.CLIENT)
    public ElectricHumSoundInstance windSoundInstance;

    public ElectricFanBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        airCurrent = new AirCurrent(this);
        updateAirFlow = true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    @Override
    public void tick() {
        float prevSpeed = actualSpeed;
        actualSpeed = calculateSpeedChange(actualSpeed, targetSpeed);

        prevRotation = rotation;
        rotation += actualSpeed * 30f;
        if (rotation > 360 || rotation < 360) {
            float diff = Mth.floor(Math.abs(rotation) / 360) * Math.signum(rotation) * 360;
            rotation -= diff;
            prevRotation -= diff;
        }

        if (Math.abs(this.actualSpeed - prevSpeed) > (Math.abs(actualSpeed) > 1 ? 1 : 0.1) ||
                (actualSpeed == 0) != (prevSpeed == 0)) {
            updateAirFlow = true;
            updateChute();
        }

        if (level.isClientSide())
            com.george_vi.electroenergetics.foundation.ClientExecution.run(() -> this::tickAudio);

        // From EncasedFanBlockEntity#tick

        boolean server = !level.isClientSide() || isVirtual();

        if (server && airCurrentUpdateCooldown-- <= 0) {
            airCurrentUpdateCooldown = AllConfigs.server().kinetics.fanBlockCheckRate.get();
            updateAirFlow = true;
        }

        if (updateAirFlow) {
            updateAirFlow = false;
            airCurrent.rebuild();
            if (airCurrent.maxDistance > 0)
                award(AllAdvancements.ENCASED_FAN);
            sendData();
        }

        if (getSpeed() == 0)
            return;

        if (entitySearchCooldown-- <= 0) {
            entitySearchCooldown = 5;
            airCurrent.findEntities();
        }

        airCurrent.tick();
    }

    @Environment(EnvType.CLIENT)
    public void tickAudio() {
        float speedNormalized = Math.abs(actualSpeed) / 10f;
        if (Math.abs(speedNormalized) < 0.1f)
            return;

        speedNormalized = speedNormalized * speedNormalized;

        if (windSoundInstance == null || windSoundInstance.isStopped()) {
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(windSoundInstance = new ElectricHumSoundInstance(CEESoundEvents.TRAIN_WIND_STATIC.get(), worldPosition));
        } else if (windSoundInstance != null && speedNormalized != 0) {
            windSoundInstance.keepAlive();
            windSoundInstance.setVolume(speedNormalized * 0.4f);
            windSoundInstance.setPitch(Mth.lerp(speedNormalized, 0.1f, 2f));
        }
    }

    public void setSpeed(float speed) {
        if (Math.abs(this.targetSpeed - speed) > (Math.abs(targetSpeed) > 1 ? 0.1 : 0.01) ||
                (targetSpeed == 0) != (speed == 0)) {
            this.targetSpeed = speed;
            if (!level.isClientSide())
                sendData();
        }
    }

    @Override
    public Direction getAirflowOriginSide() {
        return this.getBlockState()
                .getValue(ElectricFanBlock.FACING);
    }

    @Override
    public Direction getAirFlowDirection() {
        if (actualSpeed == 0)
            return null;
        Direction facing = getBlockState().getValue(ElectricFanBlock.FACING);
        return actualSpeed > 0 ? facing : facing.getOpposite();
    }

    @Override
    public boolean isSourceRemoved() {
        return remove;
    }

    @Override
    public AirCurrent getAirCurrent() {
        return airCurrent;
    }

    @Nullable
    @Override
    public Level getAirCurrentWorld() {
        return level;
    }

    @Override
    public BlockPos getAirCurrentPos() {
        return worldPosition;
    }

    @Override
    public float getSpeed() {
        return Math.abs(actualSpeed) > 1 ? actualSpeed * actualSpeed * 0.8f : 0;
    }

    @Override
    public void remove() {
        super.remove();
        updateChute();
    }

    public void updateChute() {
        Direction direction = getBlockState().getValue(ElectricFanBlock.FACING);
        if (!direction.getAxis()
                .isVertical())
            return;
        BlockEntity poweredChute = level.getBlockEntity(worldPosition.relative(direction));
        if (!(poweredChute instanceof ChuteBlockEntity chuteBE))
            return;
        if (direction == Direction.DOWN)
            chuteBE.updatePull();
        else
            chuteBE.updatePush(1);
    }

    public void blockInFrontChanged() {
        updateAirFlow = true;
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        CompoundTag tag = NbtView.read(input);

        actualSpeed = tag.getFloatOr("Speed", 0f);
        targetSpeed = tag.getFloatOr("TargetSpeed", 0f);
        if (clientPacket)
            airCurrent.rebuild();

    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Speed", actualSpeed);
        tag.putFloat("TargetSpeed", targetSpeed);

        NbtView.write(output, tag);
    }

    public static float calculateSpeedChange(float speed, float target) {
        return Mth.lerp(Math.abs(speed) < 1 ? 0.1f : 0.01f, speed, Mth.clamp(target, -10, 10));
    }
}
