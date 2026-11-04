package foundry.veil.api.network;

import foundry.veil.api.network.handler.ClientPacketContext;
import foundry.veil.api.network.handler.PacketContext;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class VeilPacketManager {
    private static final Map<CustomPacketPayload.Type<?>, ClientRegistration<?>> CLIENTBOUND = new LinkedHashMap<>();
    private static final java.util.List<java.util.function.Consumer<net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent>> REGISTRATIONS = new java.util.ArrayList<>();
    private String modId;
    private String version;

    private static final Set<CustomPacketPayload.Type<?>> REGISTERED_CLIENT_RECEIVERS = new LinkedHashSet<>();

    private VeilPacketManager() {
    }

    public static VeilPacketManager create(final String modId, final String version) {
        var manager=new VeilPacketManager();manager.modId=modId;manager.version=version;return manager;
    }

    public <T extends CustomPacketPayload> void registerClientbound(
            final CustomPacketPayload.Type<T> id,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final PacketHandler<ClientPacketContext, T> handler) {
        this.registerClientbound(id, codec, handler, false);
    }

    public <T extends CustomPacketPayload> void registerClientbound(
            final CustomPacketPayload.Type<T> id,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final PacketHandler<ClientPacketContext, T> handler,
            final boolean optional) {
        REGISTRATIONS.add(event -> {
            var registrar=event.registrar(modId).versioned(version);
            if(optional)registrar=registrar.optional();
            registrar.playToClient(id,codec);
        });
        CLIENTBOUND.put(id, new ClientRegistration<>(id, handler));
    }

    public <T extends CustomPacketPayload> void registerServerbound(
            final CustomPacketPayload.Type<T> id,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final PacketHandler<ServerPacketContext, T> handler) {
        this.registerServerbound(id, codec, handler, false);
    }

    public <T extends CustomPacketPayload> void registerServerbound(
            final CustomPacketPayload.Type<T> id,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final PacketHandler<ServerPacketContext, T> handler,
            final boolean optional) {
        REGISTRATIONS.add(event -> {
            var registrar=event.registrar(modId).versioned(version);
            if(optional)registrar=registrar.optional();
            registrar.playToServer(id,codec,(payload,context) -> handler.handlePacket(payload,new NeoForgeServerPacketContext((ServerPlayer)context.player())));
        });
    }

    public static void registerPayloads(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event){
        REGISTRATIONS.forEach(registration -> registration.accept(event));
    }

    public static void registerClientReceivers(net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent event){
        CLIENTBOUND.values().forEach(registration -> registerClientReceiver(event,registration));
    }
    private static <T extends CustomPacketPayload> void registerClientReceiver(
            net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent event,ClientRegistration<T> registration){
        event.register(registration.type(),(payload,context) -> registration.handler().handlePacket(payload,
            new NeoForgeClientPacketContext(Minecraft.getInstance(),Minecraft.getInstance().player)));
    }

    public static PacketSink server() {
        return new PacketSink() {
            @Override
            public void sendPacket(final CustomPacketPayload... payloads) {
                for (final CustomPacketPayload payload : payloads) {
                    final var connection=Minecraft.getInstance().getConnection();
                    if(connection!=null)connection.send(new ServerboundCustomPayloadPacket(payload));
                }
            }

            @Override
            public void sendPacket(final Packet<?> packet) {
                final Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.getConnection() != null) {
                    minecraft.getConnection().send(packet);
                }
            }
        };
    }

    public static PacketSink player(final ServerPlayer player) {
        return packet -> player.connection.send(packet);
    }

    public static PacketSink level(final ServerLevel level) {
        return players(level.players());
    }

    public static PacketSink around(@Nullable final ServerPlayer excluded, final ServerLevel level,
                                    final double x, final double y, final double z, final double radius) {
        final Vec3 center = new Vec3(x, y, z);
        return players(level.players().stream().filter(player -> player.distanceToSqr(center)<=radius*radius)
                .filter(player -> player != excluded)
                .toList());
    }

    public static PacketSink all(final MinecraftServer server) {
        return players(server.getPlayerList().getPlayers());
    }

    public static PacketSink tracking(final Entity entity) {
        return players(((ServerLevel)entity.level()).getChunkSource().chunkMap.getPlayersWatching(entity));
    }

    public static PacketSink trackingAndSelf(final Entity entity) {
        final Set<ServerPlayer> players = new LinkedHashSet<>(((ServerLevel)entity.level()).getChunkSource().chunkMap.getPlayersWatching(entity));
        if (entity instanceof final ServerPlayer player) {
            players.add(player);
        }
        return players(players);
    }

    public static PacketSink tracking(final ServerLevel level, final ChunkPos pos) {
        return players(level.getChunkSource().chunkMap.getPlayers(pos, false));
    }

    public static PacketSink tracking(final ServerLevel level, final BlockPos pos) {
        return tracking(level,ChunkPos.containing(pos));
    }

    public static PacketSink tracking(final ServerLevel level, final BlockPos min, final BlockPos max) {
        final Set<ServerPlayer> players = new LinkedHashSet<>();
        ChunkPos.rangeClosed(ChunkPos.containing(min), ChunkPos.containing(max))
                .forEach(pos -> players.addAll(level.getChunkSource().chunkMap.getPlayers(pos, false)));
        return players(players);
    }

    public static PacketSink tracking(final BlockEntity blockEntity) {
        return tracking((ServerLevel)blockEntity.getLevel(),blockEntity.getBlockPos());
    }

    private static PacketSink players(final Iterable<ServerPlayer> players) {
        return packet -> players.forEach(player -> player.connection.send(packet));
    }

    @FunctionalInterface
    public interface PacketHandler<T extends PacketContext, P extends CustomPacketPayload> {
        void handlePacket(P payload, T context);
    }

    @FunctionalInterface
    public interface PacketSink {
        default void sendPacket(final CustomPacketPayload... payloads) {
            for (final CustomPacketPayload payload : payloads) {
                this.sendPacket(new ClientboundCustomPayloadPacket(payload));
            }
        }

        void sendPacket(Packet<?> packet);
    }

    private record ClientRegistration<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            PacketHandler<ClientPacketContext, T> handler) {
    }

    private record NeoForgeServerPacketContext(ServerPlayer player) implements ServerPacketContext {
        @Override
        public Packet<?> createPacket(final CustomPacketPayload payload) {
            return new ClientboundCustomPayloadPacket(payload);
        }

        @Override
        public void sendPacket(final Packet<?> packet, @Nullable final PacketSendListener callback) {
            this.player.connection.send(packet);
        }

        @Override
        public void disconnect(final Component disconnectReason) {
            this.player.connection.disconnect(disconnectReason);
        }
    }

    private record NeoForgeClientPacketContext(Minecraft client, @Nullable LocalPlayer player) implements ClientPacketContext {
        @Override
        public Packet<?> createPacket(final CustomPacketPayload payload) {
            return new ServerboundCustomPayloadPacket(payload);
        }

        @Override
        public void sendPacket(final Packet<?> packet, @Nullable final PacketSendListener callback) {
            if (this.client.getConnection() != null) {
                this.client.getConnection().send(packet);
            }
        }

        @Override
        public void disconnect(final Component disconnectReason) {
            if (this.client.getConnection() != null) {
                this.client.getConnection().getConnection().disconnect(disconnectReason);
            }
        }
    }
}
