package com.george_vi.electroenergetics.foundation;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireEffects;
import io.netty.buffer.ByteBuf;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record SendSparkPacket(Vec3 pos, SparkSize size) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendSparkPacket> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.world.phys.Vec3.STREAM_CODEC, SendSparkPacket::pos,
            CatnipStreamCodecBuilders.ofEnum(SparkSize.class), SendSparkPacket::size,
            SendSparkPacket::new
    );

    @Override

    @Environment(EnvType.CLIENT)
    public void handle(Player player) {
        if (size == SparkSize.SMALL) {
            WireEffects.showSmallSpark(pos);
        } else if (size == SparkSize.MEDIUM) {
            WireEffects.showMediumSpark(pos);
        } else if (size == SparkSize.LARGE) {
            WireEffects.showLargeSpark(pos);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_SPARK;
    }

    public enum SparkSize {
        SMALL,
        MEDIUM,
        LARGE
    }
}
