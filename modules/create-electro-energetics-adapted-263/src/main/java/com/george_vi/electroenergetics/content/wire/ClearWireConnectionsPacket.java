package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import io.netty.buffer.ByteBuf;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.data.Pair;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.List;

public record ClearWireConnectionsPacket(List<Pair<InWorldNode, InWorldNode>> connections, boolean all) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, ClearWireConnectionsPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(Pair.streamCodec(InWorldNode.STREAM_CODEC, InWorldNode.STREAM_CODEC)), ClearWireConnectionsPacket::connections,
            ByteBufCodecs.BOOL, ClearWireConnectionsPacket::all,
            ClearWireConnectionsPacket::new
    );


    public static ClearWireConnectionsPacket clearAll() {
        return new ClearWireConnectionsPacket(Collections.emptyList(), true);
    }

    public static ClearWireConnectionsPacket clearWire(InWorldNode node1, InWorldNode node2) {
        return new ClearWireConnectionsPacket(List.of(Pair.of(node1, node2)), false);
    }

    public static ClearWireConnectionsPacket clearWire(InWorldNodeConnection connection) {
        return clearWire(connection.node1(), connection.node2());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Player player) {
        if (all) {
            WireRenderer.clearAllNodeData();
            WireRenderer.clearAllWireConnections();
            return;
        }

        for (Pair<InWorldNode, InWorldNode> connection : connections()) {
            WireRenderer.removeConnections(new InWorldNodeConnection(connection.getFirst(), connection.getSecond()));
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CLEAR_WIRE_CONNECTIONS;
    }
}
