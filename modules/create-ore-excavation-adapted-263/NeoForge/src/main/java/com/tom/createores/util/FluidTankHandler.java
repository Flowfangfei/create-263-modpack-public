package com.tom.createores.util;

import net.neoforged.neoforge.fluids.FluidStack;

import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Single-tank transactional fluid store with an optional input-only policy. */
public class FluidTankHandler extends FluidStacksResourceHandler {
    public enum FluidAction { EXECUTE, SIMULATE; public boolean execute() { return this == EXECUTE; } }
	private final boolean acceptsExternalInput;
	private final Runnable changed;

	public FluidTankHandler(int capacity, boolean acceptsExternalInput, Runnable changed) {
		super(1, capacity);
		this.acceptsExternalInput = acceptsExternalInput;
		this.changed = changed;
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return true;
	}

    @Override
    public int insert(int index, FluidResource resource, int amount, net.neoforged.neoforge.transfer.transaction.TransactionContext transaction) {
        return acceptsExternalInput ? super.insert(index, resource, amount, transaction) : 0;
    }
    @Override
    public int extract(int index, FluidResource resource, int amount, net.neoforged.neoforge.transfer.transaction.TransactionContext transaction) {
        return acceptsExternalInput ? 0 : super.extract(index, resource, amount, transaction);
    }
	@Override
	protected void onContentsChanged(int index, FluidStack previousContents) {
		changed.run();
	}

	public FluidStack getFluid() {
		return getResource(0).toStack(getAmountAsInt(0));
	}

	public int getFluidAmount() {
		return getAmountAsInt(0);
	}

	public int fillInternal(FluidStack stack, FluidAction action) {
		if (stack.isEmpty()) return 0;
		try (var tx = Transaction.openRoot()) {
			int inserted = super.insert(0, FluidResource.of(stack), stack.getAmount(), tx);
			if (action.execute()) tx.commit();
			return inserted;
		}
	}

	public FluidStack drainInternal(int amount, FluidAction action) {
		if (amount <= 0 || getFluid().isEmpty()) return FluidStack.EMPTY;
		FluidResource resource = getResource(0);
		try (var tx = Transaction.openRoot()) {
			int extracted = super.extract(0, resource, amount, tx);
			if (action.execute()) tx.commit();
			return extracted == 0 ? FluidStack.EMPTY : resource.toStack(extracted);
		}
	}
}
