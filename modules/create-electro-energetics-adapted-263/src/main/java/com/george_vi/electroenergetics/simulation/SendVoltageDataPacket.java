package com.george_vi.electroenergetics.simulation;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.client.NodeVoltageHolder;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.network.codec.StreamCodec;

public final class SendVoltageDataPacket implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, SendVoltageDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SendVoltageDataPacket decode(ByteBuf buffer) {
            SendVoltageDataPacket p = new SendVoltageDataPacket();
            int microTicks = p.microTicks = buffer.readShort();
            if (microTicks < 1 || microTicks > 64)
                throw new DecoderException("Received a voltage sync packet with negative micro-ticks!");

            int length = buffer.readInt();
            if (length < 0 || length > 65_536 || (long) length * microTicks > 1_048_576)
                throw new DecoderException("Voltage sync packet exceeds the permitted sample count");

            p.nodes = new InWorldNode[length];
            p.frequencies = new float[length];
            p.voltages = new double[length * microTicks];

            for (int i = 0; i < length; i++) {
                p.nodes[i] = InWorldNode.STREAM_CODEC.decode(buffer);
                p.frequencies[i] = buffer.readFloat();
                for (int j = 0; j < microTicks; j++)
                    p.voltages[i * microTicks + j] = buffer.readDouble();
            }
            return p;
        }

        @Override
        public void encode(ByteBuf buffer, SendVoltageDataPacket p) {
            int microTicks = p.microTicks;
            buffer.writeShort(microTicks);
            buffer.writeInt(p.nodes.length);

            for (int i = 0; i < p.nodes.length; i++) {
                InWorldNode.STREAM_CODEC.encode(buffer, p.nodes[i]);
                buffer.writeFloat(p.frequencies[i]);
                for (int j = 0; j < microTicks; j++)
                    buffer.writeDouble(p.voltages[i * microTicks + j]);
            }
        }
    };

    InWorldNode[] nodes;

    // a flattened 2d array
    double[] voltages;
    int microTicks;
    float[] frequencies;

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Player player) {
        for (int i = 0; i < nodes.length; i++) {
            InWorldNode node = nodes[i];
            NodeVoltageHolder.VoltageEntry ve = NodeVoltageHolder.getVoltageEntry(node);
            if (ve == null || ve == NodeVoltageHolder.VoltageEntry.ZERO) {
                ve = new NodeVoltageHolder.VoltageEntry();
                ve.frequency = frequencies[i];
                ve.voltages = new double[microTicks];
                System.arraycopy(voltages, i * microTicks, ve.voltages, 0, microTicks);

                ve.recompute();
                NodeVoltageHolder.addVoltageData(node, ve);
            } else {
                double[] list = (ve.voltages != null && ve.voltages.length == microTicks) ? ve.voltages : (ve.voltages = new double[microTicks]);

                System.arraycopy(voltages, i * microTicks, list, 0, microTicks);

                ve.frequency = frequencies[i];
                ve.recompute();
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CEEPackets.SEND_VOLTAGE_DATA;
    }
}
