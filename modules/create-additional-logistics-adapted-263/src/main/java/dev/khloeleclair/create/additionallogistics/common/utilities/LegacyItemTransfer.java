package dev.khloeleclair.create.additionallogistics.common.utilities;

import com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Transactional view over the retained Create legacy inventory API. Share one instance across sided views. */
public final class LegacyItemTransfer implements ResourceHandler<ItemResource> {
    private final IItemHandlerModifiable inventory;
    private final SnapshotJournal<ItemStack[]> journal;

    public LegacyItemTransfer(IItemHandlerModifiable inventory, Runnable changed) {
        this.inventory = inventory;
        this.journal = new SnapshotJournal<>() {
            @Override protected ItemStack[] createSnapshot() {
                ItemStack[] stacks = new ItemStack[inventory.getSlots()];
                for (int i = 0; i < stacks.length; i++) stacks[i] = inventory.getStackInSlot(i).copy();
                return stacks;
            }
            @Override protected void revertToSnapshot(ItemStack[] stacks) {
                for (int i = 0; i < stacks.length; i++) inventory.setStackInSlot(i, stacks[i]);
            }
            @Override protected void onRootCommit(ItemStack[] original) { changed.run(); }
        };
    }
    @Override public int size() { return inventory.getSlots(); }
    @Override public ItemResource getResource(int index) { return ItemResource.of(inventory.getStackInSlot(index)); }
    @Override public long getAmountAsLong(int index) { return inventory.getStackInSlot(index).getCount(); }
    @Override public long getCapacityAsLong(int index, ItemResource resource) {
        return isValid(index, resource) ? inventory.getSlotLimit(index) : 0;
    }
    @Override public boolean isValid(int index, ItemResource resource) {
        return resource.isEmpty() || inventory.isItemValid(index, resource.toStack(1));
    }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0 || !isValid(index, resource)) return 0;
        int accepted = amount - inventory.insertItem(index, resource.toStack(amount), true).getCount();
        if (accepted <= 0) return 0;
        journal.updateSnapshots(transaction);
        return accepted - inventory.insertItem(index, resource.toStack(accepted), false).getCount();
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0 || !resource.matches(inventory.getStackInSlot(index))) return 0;
        ItemStack extracted = inventory.extractItem(index, amount, true);
        if (extracted.isEmpty()) return 0;
        journal.updateSnapshots(transaction);
        return inventory.extractItem(index, extracted.getCount(), false).getCount();
    }
}