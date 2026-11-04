package com.george_vi.electroenergetics.events.network;

import net.minecraft.server.level.ServerPlayer;

public interface ServerboundPacketPayload extends BasePacketPayload {
    void handle(ServerPlayer player);
}
