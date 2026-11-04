package net.fxnt.fxntstorage.inventory;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/** Network boundary shared only by this mod's menus. Vanilla stacks and hashes remain unchanged. */
public abstract class StorageMenu extends AbstractContainerMenu {
    public static final int MAX_MENU_SLOTS = StorageContents.MAX_SLOTS + 36;
    private final @Nullable Player viewer;

    protected StorageMenu(@Nullable MenuType<?> type, int containerId, @Nullable Player viewer) {
        super(type, containerId);
        this.viewer = viewer;
    }

    @Override
    public void setSynchronizer(ContainerSynchronizer synchronizer) {
        super.setSynchronizer(viewer instanceof ServerPlayer player
                ? new StorageMenuSynchronizer(synchronizer, payload -> PacketDistributor.sendToPlayer(player, payload))
                : synchronizer);
    }

    public final boolean isPrivateSlot(int index) {
        return index >= 0 && index < slots.size() && !(slots.get(index).container instanceof Inventory);
    }

    public final boolean validSyncedStack(int index, ItemStack stack) {
        if (index < 0 || index >= slots.size()) return false;
        if (!isPrivateSlot(index)) return ordinary(stack);
        if (stack.isEmpty()) return true;
        if (stack.getCount() < 1 || stack.getCount() > StorageContents.MAX_COUNT
                || ItemStack.validateStrict(stack.copyWithCount(1)).isError()) return false;
        return validPrivateStack(index, stack);
    }

    protected boolean validPrivateStack(int index, ItemStack stack) {
        Slot slot = slots.get(index);
        return stack.getCount() <= slot.getMaxStackSize(stack);
    }

    public final boolean validInitialData(List<ItemStack> items, ItemStack carried) {
        if (items.size() != slots.size() || items.size() > MAX_MENU_SLOTS || !ordinary(carried)) return false;
        for (int index = 0; index < items.size(); index++)
            if (!validSyncedStack(index, items.get(index))) return false;
        return true;
    }

    public final boolean applyPrivateInitialData(int id, int incomingState, List<ItemStack> items, ItemStack carried) {
        if (id != containerId || !acceptsState(incomingState) || !validInitialData(items, carried)) return false;
        initializeContents(incomingState, items, carried);
        return true;
    }

    public final boolean applyPrivateSlot(int id, int incomingState, int index, ItemStack stack) {
        if (id != containerId || !acceptsState(incomingState) || !isPrivateSlot(index)
                || !validSyncedStack(index, stack)) return false;
        setItem(index, incomingState, stack);
        return true;
    }

    private boolean acceptsState(int incoming) {
        // Native state ids are 15-bit counters. Equal ids permit initial data and data-slot packets.
        return incoming >= 0 && incoming <= 32767 && ((incoming - getStateId()) & 32767) < 16384;
    }

    public static boolean ordinary(ItemStack stack) {
        return stack.isEmpty() || !ItemStack.validateStrict(stack).isError();
    }
}
