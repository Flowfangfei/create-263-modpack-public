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

import java.util.List;

public record SyncContainerPacket(int containerId, int stateId, List<ItemStack> items,
                                  ItemStack carriedItem) implements CustomPacketPayload {
    public static final Type<SyncContainerPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(FXNTStorage.MOD_ID, "sync_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncContainerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncContainerPacket::containerId,
            ByteBufCodecs.INT, SyncContainerPacket::stateId,
            StorageContents.STACK_STREAM_CODEC.apply(ByteBufCodecs.list(StorageMenu.MAX_MENU_SLOTS)), SyncContainerPacket::items,
            ItemStack.OPTIONAL_STREAM_CODEC, SyncContainerPacket::carriedItem,
            SyncContainerPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof Player player && player.level().isClientSide()
                    && player.containerMenu instanceof StorageMenu menu)
                menu.applyPrivateInitialData(containerId(), stateId(), items(), carriedItem());
        });
    }
}
