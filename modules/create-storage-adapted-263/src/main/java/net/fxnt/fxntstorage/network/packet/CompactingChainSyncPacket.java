package net.fxnt.fxntstorage.network.packet;

import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.simple_storage.CompactingChain;
import net.fxnt.fxntstorage.simple_storage.CompactingRecipeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Server-authoritative compaction chains, since client recipe access has no full recipe collection. */
public record CompactingChainSyncPacket(Identifier filter, boolean response, List<Identifier> tiers,
        List<Integer> ratios, List<Identifier> lowerVariants, List<Identifier> upperVariants) implements CustomPacketPayload {
    public static final Type<CompactingChainSyncPacket> TYPE = new Type<>(FXNTStorage.modLoc("compacting_chain"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CompactingChainSyncPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CompactingChainSyncPacket::filter,
            ByteBufCodecs.BOOL, CompactingChainSyncPacket::response,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), CompactingChainSyncPacket::tiers,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), CompactingChainSyncPacket::ratios,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), CompactingChainSyncPacket::lowerVariants,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), CompactingChainSyncPacket::upperVariants,
            CompactingChainSyncPacket::new);
    public static CompactingChainSyncPacket request(Item item) {
        return new CompactingChainSyncPacket(BuiltInRegistries.ITEM.getKey(item), false, List.of(), List.of(), List.of(), List.of());
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Item item = BuiltInRegistries.ITEM.getValue(filter);
            if (item == null) return;
            if (context.player() instanceof ServerPlayer player) {
                if (response) return;
                CompactingChain chain = CompactingRecipeHelper.buildChain(player.level(), item);
                PacketDistributor.sendToPlayer(player, reply(item, chain));
            } else if (response) {
                CompactingChain chain = null;
                if ((tiers.size() == 2 || tiers.size() == 3) && ratios.size() == 2 && ratios.get(0) > 0 && ratios.get(1) > 0)
                    chain = new CompactingChain(resolve(tiers.get(0)), resolve(tiers.get(1)), ratios.get(0),
                            tiers.size() == 3 ? resolve(tiers.get(2)) : null, ratios.get(1), items(lowerVariants), items(upperVariants));
                CompactingRecipeHelper.receiveClientChain(item, chain);
            }
        });
    }
    private static CompactingChainSyncPacket reply(Item item, CompactingChain chain) {
        if (chain == null) return new CompactingChainSyncPacket(BuiltInRegistries.ITEM.getKey(item), true, List.of(), List.of(), List.of(), List.of());
        List<Identifier> tiers = chain.t2() == null ? List.of(id(chain.t0()), id(chain.t1()))
                : List.of(id(chain.t0()), id(chain.t1()), id(chain.t2()));
        return new CompactingChainSyncPacket(id(item), true, tiers, List.of(chain.t0ToT1(), chain.t1ToT2()),
                chain.t0Variants().stream().map(CompactingChainSyncPacket::id).toList(),
                chain.t1Variants().stream().map(CompactingChainSyncPacket::id).toList());
    }
    private static Identifier id(Item item) { return BuiltInRegistries.ITEM.getKey(item); }
    private static Item resolve(Identifier id) { return BuiltInRegistries.ITEM.getValue(id); }
    private static Set<Item> items(List<Identifier> ids) { return ids.stream().map(CompactingChainSyncPacket::resolve).filter(java.util.Objects::nonNull).collect(Collectors.toSet()); }
}
