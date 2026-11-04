package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.foundation.nodes.DirectionalInWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import io.netty.buffer.ByteBuf;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.data.Pair;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record SendWireConnectionsPacket(List<Pair<DirectionalInWorldNodeConnection, WireData>> connections) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SendWireConnectionsPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(Pair.streamCodec(DirectionalInWorldNodeConnection.STREAM_CODEC, WireData.STREAM_CODEC)), SendWireConnectionsPacket::connections,
            SendWireConnectionsPacket::new
    );

    public static SendWireConnectionsPacket connectWire(InWorldNode node1, InWorldNode node2, WireData data) {
        return new SendWireConnectionsPacket(List.of(Pair.of(new DirectionalInWorldNodeConnection(node1, node2), data)));
    }

    public static SendWireConnectionsPacket connectWire(InWorldNodeConnection connection, WireData data) {
        return connectWire(connection.node1(), connection.node2(), data);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Player player) {
        for (Pair<DirectionalInWorldNodeConnection, WireData> connection : connections())
            WireRenderer.addConnection(new InWorldNodeConnection(connection.getFirst().node1(), connection.getFirst().node2()), connection.getSecond());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_WIRE_CONNECTIONS;
    }
}
