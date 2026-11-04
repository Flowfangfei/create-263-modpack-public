package com.george_vi.electroenergetics.events.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

public interface ClientboundPacketPayload extends BasePacketPayload {
    @Environment(EnvType.CLIENT)
    void handle(Player player);
}
