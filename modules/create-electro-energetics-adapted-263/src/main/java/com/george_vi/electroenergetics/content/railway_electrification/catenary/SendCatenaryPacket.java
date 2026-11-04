package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireRenderer;
import io.netty.buffer.ByteBuf;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record SendCatenaryPacket(List<CatenaryConnection> connections) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendCatenaryPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(CatenaryConnection.STREAM_CODEC), SendCatenaryPacket::connections,
            SendCatenaryPacket::new
    );

    public static SendCatenaryPacket connectWire(CatenaryConnection connection) {
        return new SendCatenaryPacket(List.of(connection));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Player player) {
        for (CatenaryConnection connection : connections())
            WireRenderer.addCatenary(connection);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_CATENARY;
    }
}
