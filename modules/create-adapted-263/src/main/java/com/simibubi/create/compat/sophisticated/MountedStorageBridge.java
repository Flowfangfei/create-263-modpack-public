package com.simibubi.create.compat.sophisticated;

import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

public abstract class MountedStorageBridge extends MountedItemStorage implements SyncedMountedStorage {
	private final ItemStack storageItem;

	protected MountedStorageBridge(MountedItemStorageType<?> type, ItemStack storageItem) {
		super(type);
		this.storageItem = storageItem;
	}

	public ItemStack getStorageStack() {
		return storageItem;
	}

	public abstract IStorageWrapper getStorageWrapper();

	public abstract void updateWithSyncedStorageStack(ItemStack storageItem, boolean update);

	private ResourceHandler<ItemResource> inventory() {
		return getStorageWrapper().getInventoryForInputOutput();
	}

	@Override
	public int getSlots() {
		return inventory().size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ResourceHandler<ItemResource> inventory = inventory();
		return inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot));
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		InventoryHelper.set(inventory(), slot, ItemResource.of(stack), stack.getCount());
	}

	@Override
	public int getSlotLimit(int slot) {
		return inventory().getCapacityAsInt(slot, ItemResource.EMPTY);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return inventory().isValid(slot, ItemResource.of(stack));
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		try (Transaction transaction = Transaction.openRoot()) {
			int inserted = inventory().insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
			if (!simulate)
				transaction.commit();
			return stack.copyWithCount(stack.getCount() - inserted);
		}
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount <= 0)
			return ItemStack.EMPTY;
		ResourceHandler<ItemResource> inventory = inventory();
		ItemResource resource = inventory.getResource(slot);
		try (Transaction transaction = Transaction.openRoot()) {
			int extracted = inventory.extract(slot, resource, amount, transaction);
			if (!simulate)
				transaction.commit();
			return resource.toStack(extracted);
		}
	}
}
