package net.fxnt.fxntstorage.inventory;

import java.util.List;
import java.util.function.Consumer;
import net.fxnt.fxntstorage.network.packet.SyncContainerPacket;
import net.fxnt.fxntstorage.network.packet.SyncSlotCountPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.item.ItemStack;

/** Reuses vanilla remote hashes/data/player slots while private quantities use count-one prototypes. */
public final class StorageMenuSynchronizer implements ContainerSynchronizer {
    private final ContainerSynchronizer delegate;
    private final Consumer<CustomPacketPayload> send;

    public StorageMenuSynchronizer(ContainerSynchronizer delegate, Consumer<CustomPacketPayload> send) {
        this.delegate = delegate;
        this.send = send;
    }

    @Override
    public void sendInitialData(AbstractContainerMenu container, List<ItemStack> items, ItemStack carried, int[] data) {
        StorageMenu menu = menu(container);
        if (!menu.validInitialData(items, carried)) throw new IllegalArgumentException("Invalid Storage menu initial stacks");
        // The entire menu includes player slots; those and the cursor are checked against native limits.
        // Sending a native full-content packet would turn the private quantities into empty client stacks.
        send.accept(new SyncContainerPacket(menu.containerId, menu.incrementStateId(),
                items.stream().map(ItemStack::copy).toList(), carried.copy()));
        for (int index = 0; index < data.length; index++) delegate.sendDataChange(menu, index, data[index]);
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu container, int index, ItemStack stack) {
        StorageMenu menu = menu(container);
        if (!menu.validSyncedStack(index, stack)) throw new IllegalArgumentException("Invalid Storage menu slot " + index);
        if (menu.isPrivateSlot(index))
            send.accept(new SyncSlotCountPacket(menu.containerId, menu.incrementStateId(), index, stack.copy()));
        else delegate.sendSlotChange(menu, index, stack);
    }

    @Override
    public void sendCarriedChange(AbstractContainerMenu container, ItemStack stack) {
        if (!StorageMenu.ordinary(stack)) throw new IllegalArgumentException("Oversized Storage menu cursor");
        delegate.sendCarriedChange(container, stack);
    }

    @Override
    public void sendDataChange(AbstractContainerMenu container, int id, int value) {
        delegate.sendDataChange(container, id, value);
    }

    @Override
    public RemoteSlot createSlot() {
        // Native HashedStack uses a varint count and already compares a private amount losslessly.
        return delegate.createSlot();
    }

    private static StorageMenu menu(AbstractContainerMenu container) {
        if (!(container instanceof StorageMenu menu)) throw new IllegalArgumentException("Not a Storage menu");
        return menu;
    }
}
