package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.catnip.math.VecHelper;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record SendQuadraticParticlesPacket(Vec3 pos1, Vec3 pos2, ParticleOptions options, Float sag, float chance) implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SendQuadraticParticlesPacket> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.world.phys.Vec3.STREAM_CODEC, SendQuadraticParticlesPacket::pos1,
            net.minecraft.world.phys.Vec3.STREAM_CODEC, SendQuadraticParticlesPacket::pos2,
            ParticleTypes.STREAM_CODEC, SendQuadraticParticlesPacket::options,
            ByteBufCodecs.FLOAT, SendQuadraticParticlesPacket::sag,
            ByteBufCodecs.FLOAT, SendQuadraticParticlesPacket::chance,
            SendQuadraticParticlesPacket::new
    );

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Player player) {
        List<Vec3> cablePoints = QuadraticWireHelper.cablePoints(pos1, pos2, sag);
        cablePoints.add(pos2);
        for (int i = 0; i < cablePoints.size() - 1; i++) {
            Vec3 point = cablePoints.get(i);
            Vec3 nextPoint = cablePoints.get(i + 1);
            for (int j = 0; j < chance; j++) {
                if (player.level().getRandom().nextFloat() > 1 - chance) {
                    point = VecHelper.lerp(player.level().getRandom().nextFloat(), point, nextPoint);
                    player.level().addParticle(options, point.x, point.y, point.z, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_QUADRATIC_PARTICLES;
    }
}
