package com.george_vi.electroenergetics.events.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Common payload contract; transport and direction are registered by CEEPackets. */
public interface BasePacketPayload extends CustomPacketPayload {
    PacketTypeProvider getTypeProvider();

    @Override
    default Type<? extends CustomPacketPayload> type() {
        return getTypeProvider().getType();
    }

    interface PacketTypeProvider {
        <T extends CustomPacketPayload> Type<T> getType();
    }
}
