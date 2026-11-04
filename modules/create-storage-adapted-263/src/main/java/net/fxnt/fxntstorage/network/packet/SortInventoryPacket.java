package net.fxnt.fxntstorage.network.packet;

import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpack.client.menu.BackpackMenu;
import net.fxnt.fxntstorage.container.StorageBoxMenu;
import net.fxnt.fxntstorage.container.mounted.StorageBoxMountedMenu;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxMenu;
import net.fxnt.fxntstorage.util.SortOrder;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SortInventoryPacket(byte invType, int slotStart, int slotEnd, SortOrder sortOrder) implements CustomPacketPayload {
    public static final Type<SortInventoryPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(FXNTStorage.MOD_ID, "sort_inventory"));

    public static final StreamCodec<FriendlyByteBuf, SortInventoryPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, SortInventoryPacket::invType,
            ByteBufCodecs.INT, SortInventoryPacket::slotStart,
            ByteBufCodecs.INT, SortInventoryPacket::slotEnd,
            SortOrder.STREAM_CODEC, SortInventoryPacket::sortOrder,
            SortInventoryPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private boolean validRange(int storageEnd, int playerStart) {
        return (slotStart == 0 && slotEnd == storageEnd)
                || (slotStart == playerStart && slotEnd == playerStart + 27)
                || (slotStart == playerStart + 27 && slotEnd == playerStart + 36);
    }

    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()
                    || !player.containerMenu.stillValid(player)) return;
            if (invType() == Util.INV_TYPE_BACKPACK && player.containerMenu instanceof BackpackMenu menu) {
                var range = net.fxnt.fxntstorage.backpack.inventory.BackpackSlotLayout.createLayout().getSortRangeForSlot(slotStart());
                if (range.isValid() && range.getStartIndex() == slotStart() && range.getEndIndex() == slotEnd())
                    menu.sortBackpackItems(slotStart(), sortOrder());
            }
            if (invType() == Util.INV_TYPE_STORAGE_BOX) {
                if (player.containerMenu instanceof StorageBoxMenu menu
                        && validRange(menu.getContainerSize(), menu.getContainerSize()))
                    menu.sortStorageItems(slotStart(), slotEnd(), sortOrder());
                else if (player.containerMenu instanceof StorageBoxMountedMenu menu
                        && validRange(menu.getContainerSize(), menu.getContainerSize()))
                    menu.sortStorageItems(slotStart(), slotEnd(), sortOrder());
                else if (player.containerMenu instanceof ReserveStorageBoxMenu menu
                        && validRange(ReserveStorageBoxMenu.STORAGE_SLOT_COUNT, ReserveStorageBoxMenu.TOTAL_CONTAINER_SLOTS))
                    menu.sortStorageItems(slotStart(), slotEnd(), sortOrder());
            }
        });
    }
}
