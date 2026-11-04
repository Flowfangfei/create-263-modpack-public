package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireRenderer;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
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
    public void handle(net.minecraft.world.entity.player.Player recipient) {
        net.minecraft.world.entity.player.Player player = recipient;
        for (CatenaryConnection connection : connections())
            WireRenderer.addCatenary(connection);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_CATENARY;
    }
}
