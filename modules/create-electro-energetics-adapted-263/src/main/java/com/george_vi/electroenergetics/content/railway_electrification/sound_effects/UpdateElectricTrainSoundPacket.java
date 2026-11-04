package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.CEERegistries;
import io.netty.buffer.ByteBuf;
import com.zurrtum.create.catnip.data.Pair;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record UpdateElectricTrainSoundPacket(UUID trainUUID, int carriageID, float speed, float acceleration, boolean active, int soundType) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, UpdateElectricTrainSoundPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public UpdateElectricTrainSoundPacket decode(ByteBuf buffer) {
            UUID uuid = UUIDUtil.STREAM_CODEC.decode(buffer);
            int carriage = buffer.readInt();
            float speed = buffer.readFloat();
            float acceleration = buffer.readFloat();
            boolean active = buffer.readBoolean();
            int soundType = buffer.readInt();
            return new UpdateElectricTrainSoundPacket(uuid, carriage, speed, acceleration, active, soundType);
        }

        @Override
        public void encode(ByteBuf buffer, UpdateElectricTrainSoundPacket p) {
            UUIDUtil.STREAM_CODEC.encode(buffer, p.trainUUID);
            buffer.writeInt(p.carriageID);
            buffer.writeFloat(p.speed);
            buffer.writeFloat(p.acceleration);
            buffer.writeBoolean(p.active);
            buffer.writeInt(p.soundType);
        }
    };

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Player player) {
        if (!active)
            ElectricTrainSounds.soundProperties.remove(Pair.of(trainUUID, carriageID));
        else
            ElectricTrainSounds.soundProperties.put(Pair.of(trainUUID, carriageID), new ElectricTrainSoundEntry(speed, acceleration, active, 7, CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.byId(soundType)));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.UPDATE_ELECTRIC_TRAIN_SOUND;
    }
}
