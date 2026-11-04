package com.george_vi.electroenergetics.events.network;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Fabric transport. Receivers always dispatch game mutations on the game thread. */
public final class CEENetwork {
    public static final CEENetwork NETWORK = new CEENetwork();

    private CEENetwork() {}

    public void sendToClient(ServerPlayer player, BasePacketPayload payload) {
        if (!(payload instanceof ClientboundPacketPayload))
            throw new IllegalArgumentException("Cannot send a serverbound payload to a client");
        ServerPlayNetworking.send(player, payload);
    }

    public void sendToClientsAround(ServerLevel level, Vec3 position, double radius, BasePacketPayload payload) {
        double squaredRadius = radius * radius;
        for (ServerPlayer player : level.players())
            if (player.position().distanceToSqr(position) <= squaredRadius)
                sendToClient(player, payload);
    }

    public void sendToClientsAround(ServerLevel level, BlockPos position, double radius, BasePacketPayload payload) {
        sendToClientsAround(level, Vec3.atCenterOf(position), radius, payload);
    }

    public void sendToAllClients(net.minecraft.server.MinecraftServer server, BasePacketPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            sendToClient(player, payload);
    }

    @Environment(EnvType.CLIENT)
    public void sendToServer(BasePacketPayload payload) {
        if (!(payload instanceof ServerboundPacketPayload))
            throw new IllegalArgumentException("Cannot send a clientbound payload to a server");
        ClientPlayNetworking.send(payload);
    }

    public static boolean canModify(ServerPlayer player, BlockPos position, double radius) {
        return !player.isSpectator() && player.isAlive() && player.level().hasChunkAt(position)
            && player.mayInteract(player.level(), position)
            && player.position().distanceToSqr(Vec3.atCenterOf(position)) <= radius * radius;
    }

    public static boolean canReachNode(ServerPlayer player, com.george_vi.electroenergetics.foundation.nodes.InWorldNode node) {
        if (player.isSpectator() || !player.isAlive()) return false;
        var data = com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData.load(player.level());
        if (!data.hasNode(node)) return false;
        Vec3 position = data.getNodePosition(node);
        double reach = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE) + 1;
        return position != null && player.mayInteract(player.level(), BlockPos.containing(position))
            && player.getEyePosition().distanceToSqr(position) <= reach * reach;
    }

    public static boolean canReachWire(ServerPlayer player, com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint point) {
        if (player.isSpectator() || !player.isAlive() || !Float.isFinite(point.point()) || point.point() < 0 || point.point() > 1)
            return false;
        var data = com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData.load(player.level());
        var wire = data.getConnectionData(point.connection());
        if (wire == null || !data.hasNode(point.node1()) || !data.hasNode(point.node2())) return false;
        Vec3 first = data.getNodePosition(point.node1());
        Vec3 second = data.getNodePosition(point.node2());
        if (wire == null || first == null || second == null) return false;
        if (!player.mayInteract(player.level(), BlockPos.containing(first)) ||
            !player.mayInteract(player.level(), BlockPos.containing(second))) return false;
        double reach = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE) + 1;
        return player.getEyePosition().distanceToSqr(point.posAt(first, second, wire.getSag())) <= reach * reach;
    }
}
