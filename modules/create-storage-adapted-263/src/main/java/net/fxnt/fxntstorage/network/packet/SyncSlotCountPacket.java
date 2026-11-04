package net.fxnt.fxntstorage.network.packet;

import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.inventory.StorageContents;
import net.fxnt.fxntstorage.inventory.StorageMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSlotCountPacket(int containerId, int stateId, int slot,
                                  ItemStack stack) implements CustomPacketPayload {
    public static final Type<SyncSlotCountPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(FXNTStorage.MOD_ID, "sync_slot_count"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSlotCountPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncSlotCountPacket::containerId,
            ByteBufCodecs.INT, SyncSlotCountPacket::stateId,
            ByteBufCodecs.INT, SyncSlotCountPacket::slot,
            StorageContents.STACK_STREAM_CODEC, SyncSlotCountPacket::stack,
            SyncSlotCountPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof Player player && player.level().isClientSide()
                    && player.containerMenu instanceof StorageMenu menu)
                menu.applyPrivateSlot(containerId(), stateId(), slot(), stack());
        });
    }
}
