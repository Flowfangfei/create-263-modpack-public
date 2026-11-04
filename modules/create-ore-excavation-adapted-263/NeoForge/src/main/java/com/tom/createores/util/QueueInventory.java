package com.tom.createores.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Output-only FIFO used by the drill. Automation can only extract from it;
 * external changes are retained only when their NeoForge transaction commits.
 */
public class QueueInventory extends SnapshotJournal<List<ItemStack>> implements ResourceHandler<ItemResource> {
	private final List<ItemStack> content = new ArrayList<>();
	private final Runnable changed;

	public QueueInventory(Runnable changed) {
		this.changed = changed;
	}

	@Override
	public int size() {
		return content.size();
	}

	@Override
	public ItemResource getResource(int index) {
		return index >= 0 && index < content.size() ? ItemResource.of(content.get(index)) : ItemResource.EMPTY;
	}

	@Override
	public long getAmountAsLong(int index) {
		return index >= 0 && index < content.size() ? content.get(index).getCount() : 0;
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return 0;
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		TransferPreconditions.checkNonEmpty(resource);
		return false;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		return 0;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
		if (index < 0 || index >= content.size() || !resource.matches(content.get(index)))
			return 0;

		int extracted = Math.min(amount, content.get(index).getCount());
		if (extracted == 0)
			return 0;

		updateSnapshots(transaction);
		ItemStack stack = content.get(index);
		stack.shrink(extracted);
		if (stack.isEmpty())
			content.remove(index);
		return extracted;
	}

	public void add(ItemStack stack) {
		if (!stack.isEmpty()) {
			content.add(stack.copy());
			changed.run();
		}
	}

	public List<ItemStack> stacks() {
		return List.copyOf(content);
	}

	public void loadStacks(List<ItemStack> stacks) {
		content.clear();
		stacks.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).forEach(content::add);
	}

	public ListTag toTag(HolderLookup.Provider registries) {
		ListTag tag = new ListTag();
		var ops = registries.createSerializationContext(NbtOps.INSTANCE);
		content.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> {
			CompoundTag encoded = new CompoundTag();
			encoded.store("stack", ItemStack.CODEC, ops, stack);
			tag.add(encoded);
		});
		return tag;
	}

	public void load(ListTag tag, HolderLookup.Provider registries) {
		content.clear();
		var ops = registries.createSerializationContext(NbtOps.INSTANCE);
		for (int i = 0; i < tag.size(); i++)
			tag.getCompound(i).flatMap(entry -> entry.read("stack", ItemStack.CODEC, ops)).ifPresent(content::add);
	}

	public boolean hasSpace() {
		content.removeIf(ItemStack::isEmpty);
		return content.size() < 16;
	}

	@Override
	protected List<ItemStack> createSnapshot() {
		return content.stream().map(ItemStack::copy).toList();
	}

	@Override
	protected void revertToSnapshot(List<ItemStack> snapshot) {
		content.clear();
		content.addAll(snapshot);
	}

	@Override
	protected void onRootCommit(List<ItemStack> originalState) {
		changed.run();
	}
}
