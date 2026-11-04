package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.WireRenderer;
import io.netty.buffer.ByteBuf;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.List;

public record ClearCatenaryPacket(List<CatenaryConnection> connections, boolean all) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, ClearCatenaryPacket> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(CatenaryConnection.STREAM_CODEC), ClearCatenaryPacket::connections,
            ByteBufCodecs.BOOL, ClearCatenaryPacket::all,
            ClearCatenaryPacket::new
    );


    public static ClearCatenaryPacket clearAll() {
        return new ClearCatenaryPacket(Collections.emptyList(), true);
    }

    public static ClearCatenaryPacket clearWire(CatenaryConnection connection) {
        return new ClearCatenaryPacket(List.of(connection), false);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Player player) {
        if (all) {
            WireRenderer.clearAllCatenaryConnections();
            return;
        }

        for (CatenaryConnection connection : connections()) {
            WireRenderer.removeCatenary(connection);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.CLEAR_CATENARY;
    }
}
