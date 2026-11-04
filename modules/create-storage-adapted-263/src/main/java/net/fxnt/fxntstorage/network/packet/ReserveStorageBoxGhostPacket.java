package net.fxnt.fxntstorage.network.packet;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.inventory.StorageContents;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxEntity;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxMenu;
import net.fxnt.fxntstorage.reserve_storage.mounted.ReserveStorageBoxMountedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record ReserveStorageBoxGhostPacket(ItemStack item, int slot, Optional<Integer> contraptionId,
                                           Optional<BlockPos> localPos, int containerId) implements CustomPacketPayload {

    public static final Type<ReserveStorageBoxGhostPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(FXNTStorage.MOD_ID, "reserve_storage_ghost"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReserveStorageBoxGhostPacket> STREAM_CODEC = StreamCodec.composite(
            StorageContents.STACK_STREAM_CODEC, ReserveStorageBoxGhostPacket::item,
            ByteBufCodecs.INT, ReserveStorageBoxGhostPacket::slot,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ReserveStorageBoxGhostPacket::contraptionId,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), ReserveStorageBoxGhostPacket::localPos,
            ByteBufCodecs.INT, ReserveStorageBoxGhostPacket::containerId,
            ReserveStorageBoxGhostPacket::new
    );

    public static ReserveStorageBoxGhostPacket forBlock(ItemStack item, int slot, int containerId) {
        return new ReserveStorageBoxGhostPacket(item, slot, Optional.empty(), Optional.empty(), containerId);
    }

    public static ReserveStorageBoxGhostPacket forMounted(ItemStack item, int slot, int contraptionId, BlockPos localPos, int containerId) {
        return new ReserveStorageBoxGhostPacket(item, slot, Optional.of(contraptionId), Optional.of(localPos), containerId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
            if (!(player.containerMenu instanceof ReserveStorageBoxMenu menu) || menu.containerId != containerId
                    || !menu.stillValid(player)) return;
            if (slot < ReserveStorageBoxMenu.STORAGE_SLOT_COUNT || slot >= ReserveStorageBoxMenu.TOTAL_CONTAINER_SLOTS) return;
            if (!item.isEmpty() && (!PacketInventorySafety.prototype(item) || item.getCount() <= 0
                    || item.getCount() > (long) ReserveStorageBoxMenu.STORAGE_SLOT_COUNT * item.getMaxStackSize())) return;
            if (contraptionId.isPresent() != localPos.isPresent() || menu.isMounted != contraptionId.isPresent()) return;

            if (menu.isMounted) {
                if (menu.contraptionId != contraptionId.orElse(-1) || !java.util.Objects.equals(menu.localPos, localPos.orElse(null))) return;
                Entity entity = player.level().getEntity(menu.contraptionId);
                if (!(entity instanceof AbstractContraptionEntity contraptionEntity) || entity.isRemoved()
                        || contraptionEntity.getContraption() == null) return;
                MountedItemStorage storage = contraptionEntity.getContraption().getStorage()
                        .getAllItemStorages().get(menu.localPos);
                if (!(storage instanceof ReserveStorageBoxMountedStorage mountedStorage)
                        || mountedStorage.isGhostDuplicate(slot, item)) return;
                mountedStorage.setGhostSlot(slot, item.copy());
            } else {
                if (menu.blockEntity == null || menu.blockEntity.isRemoved() || menu.isGhostDuplicate(slot, item)) return;
                menu.blockEntity.getItemHandler().setStackInSlot(slot, item.copy());
                menu.blockEntity.setChanged();
            }
        });
    }
}
