package net.fxnt.fxntstorage.network.packet;

import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.client.menu.BackpackMenu;
import net.fxnt.fxntstorage.backpack.inventory.BackpackSlotLayout;
import net.fxnt.fxntstorage.backpack.upgrade.GhostFilterHelper;
import net.fxnt.fxntstorage.inventory.StorageContents;
import net.minecraft.world.item.ItemStack;

/** Bounds at the boundary between private display quantities and ordinary game slots. */
final class PacketInventorySafety {
    private PacketInventorySafety() {}

    static boolean ordinary(ItemStack stack) {
        return stack.isEmpty() || !ItemStack.validateStrict(stack).isError();
    }

    static boolean prototype(ItemStack stack) {
        return stack.isEmpty() || !ItemStack.validateStrict(stack.copyWithCount(1)).isError();
    }

    static boolean backpackSlot(BackpackMenu menu, int slot, ItemStack stack) {
        if (slot < 0 || slot >= menu.container.getItemHandler().getSlots()) return false;
        if (stack.isEmpty()) return true;
        if (!prototype(stack)) return false;
        BackpackSlotLayout layout = BackpackSlotLayout.createLayout();
        if (layout.items().contains(slot)) {
            long limit = (long) stack.getMaxStackSize() * menu.container.getStackMultiplier();
            return !(stack.getItem() instanceof BackpackItem) && stack.getCount() > 0
                    && stack.getCount() <= Math.min(limit, StorageContents.MAX_COUNT);
        }
        if (GhostFilterHelper.upgradeForSlot(layout, slot) != null) return stack.getCount() == 1;
        return ordinary(stack);
    }
}
