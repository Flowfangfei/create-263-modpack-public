package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.entity.Train;
import io.netty.buffer.ByteBuf;
import com.george_vi.electroenergetics.events.network.ServerboundPacketPayload;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ChangeTrainSoundTypePacket(UUID uuid, int soundId) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, ChangeTrainSoundTypePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ChangeTrainSoundTypePacket::uuid,
            ByteBufCodecs.INT, ChangeTrainSoundTypePacket::soundId,
            ChangeTrainSoundTypePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        Train train = Create.RAILWAYS.trains.get(uuid);
        if (train == null) return;
        if (train.owner != null && !train.owner.equals(player.getUUID()) &&
            !player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) return;
        if (!(train instanceof ICEETrainExtension trainExtension))
            return;
        var optionalSoundType = CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.get(soundId);
        if (optionalSoundType.isEmpty())
            return;
        trainExtension.setSoundType(optionalSoundType.get().value());
        Create.RAILWAYS.markTracksDirty();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CHANGE_TRAIN_SOUND_TYPE;
    }
}
