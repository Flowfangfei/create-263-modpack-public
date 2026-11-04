package net.fxnt.fxntstorage.network.packet;

import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpack.client.menu.BackpackMenu;
import net.fxnt.fxntstorage.backpack.inventory.BackpackSlotLayout;
import net.fxnt.fxntstorage.backpack.upgrade.GhostFilterHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GhostItemPacket(ItemStack item, int slot, int containerId) implements CustomPacketPayload {
    public static final Type<GhostItemPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(FXNTStorage.MOD_ID, "ghost_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GhostItemPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, GhostItemPacket::item,
            ByteBufCodecs.INT, GhostItemPacket::slot,
            ByteBufCodecs.INT, GhostItemPacket::containerId,
            GhostItemPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
            if (!(player.containerMenu instanceof BackpackMenu menu) || menu.containerId != containerId
                    || !menu.stillValid(player)) return;
            var itemHandler = menu.container.getItemHandler();
            if (slot < 0 || slot >= itemHandler.getSlots() || slot >= menu.slots.size()) return;
            var upgrade = GhostFilterHelper.upgradeForSlot(BackpackSlotLayout.createLayout(), slot);
            if (upgrade == null || !menu.hasUpgrade(upgrade.getType()) || !menu.getSlot(slot).isActive()) return;
            // Create filters are real, recoverable items. Their transfer belongs to the ordinary click path.
            if (GhostFilterHelper.isRealFilterItem(item) || GhostFilterHelper.isRealFilterItem(itemHandler.getStackInSlot(slot))) return;
            if (!item.isEmpty() && (item.getCount() != 1 || !PacketInventorySafety.ordinary(item)
                    || !GhostFilterHelper.accepts(menu, slot, item))) return;
            itemHandler.setStackInSlot(slot, item.copy());
            menu.container.setDataChanged();
        });
    }
}
