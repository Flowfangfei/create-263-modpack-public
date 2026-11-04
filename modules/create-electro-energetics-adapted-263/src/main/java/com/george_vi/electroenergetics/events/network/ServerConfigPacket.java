package com.george_vi.electroenergetics.events.network;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerConfigPacket(String json) implements CustomPacketPayload {
    public static final Type<ServerConfigPacket> TYPE = new Type<>(CreateElectroEnergetics.rl("server_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(65_536), ServerConfigPacket::json, ServerConfigPacket::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            ServerPlayNetworking.send(handler.getPlayer(), new ServerConfigPacket(CEEConfigs.server().snapshot().toString())));
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        CEEClientNetworking.registerServerConfig();
    }
}
