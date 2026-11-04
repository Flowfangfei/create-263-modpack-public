package com.george_vi.electroenergetics.events.network;

import com.george_vi.electroenergetics.CEEPackets;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Kept in a client-only class so receiver lambdas never enter dedicated-server verification. */
@Environment(EnvType.CLIENT)
public final class CEEClientNetworking {
    private CEEClientNetworking() {}

    public static void register() {
        for (CEEPackets packet : CEEPackets.values()) {
            if (!packet.isServerbound())
                ClientPlayNetworking.registerGlobalReceiver(packet.<ClientboundPacketPayload>getType(), (payload, context) ->
                    context.client().execute(() -> {
                        if (context.client().player != null) payload.handle(context.client().player);
                    }));
        }
        registerServerConfig();
    }

    public static void registerServerConfig() {
        ClientPlayNetworking.registerGlobalReceiver(ServerConfigPacket.TYPE, (payload, context) -> context.client().execute(() ->
            CEEConfigs.applyServerConfig(JsonParser.parseString(payload.json()).getAsJsonObject())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CEEConfigs.clearServerConfig());
    }
}
