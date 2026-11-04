package com.yision.fluidlogistics.content.fluids.multiFluidTank;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import net.neoforged.neoforge.fluids.FluidStack;
import com.simibubi.create.compat.neoforge263.fluids.IFluidTank;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.nbt.NbtOps;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;

public class SmartMultiFluidTank implements IFluidHandler, IFluidTank, SharedCapacityFluidHandler, ResourceHandler<FluidResource> {

	private final Consumer<FluidStack[]> updateCallback;
	private final int tankCount;
	protected int capacity;
	protected Predicate<FluidStack> validator;

	@NotNull
	protected FluidStack[] fluids;

	public SmartMultiFluidTank(int capacity, int tankCount, Consumer<FluidStack[]> updateCallback) {
		this.capacity = capacity;
		this.validator = e -> true;
		this.updateCallback = updateCallback;
		this.tankCount = tankCount;
		fluids = new FluidStack[tankCount];
		resetTanks();
	}

	public SmartMultiFluidTank setCapacity(int capacity) {
		this.capacity = capacity;
		return this;
	}

	public SmartMultiFluidTank setValidator(Predicate<FluidStack> validator) {
		if (validator != null) {
			this.validator = validator;
		}
		return this;
	}

	@Override
	public int getFluidAmount() {
		int amount = 0;
		for (int i = 0; i < getTanks(); i++) {
			amount += fluids[i].getAmount();
		}
		return amount;
	}

	public int getFluidAmount(int tank) {
		if (tank >= 0 && tank < getTanks())
			return fluids[tank].getAmount();

		return 0;
	}

	public void resetTanks() {
		for (int i = 0; i < getTanks(); i++) {
			fluids[i] = FluidStack.EMPTY;
		}
	}

	protected void onContentsChanged() {
		updateCallback.accept(getFluids());
	}

	public void setFluid(FluidStack stack) {
		for (int i = 1; i < tankCount; i++) {
			fluids[i] = FluidStack.EMPTY;
		}
		setFluid(0, stack);
	}

	public void setFluid(int tank, FluidStack stack) {
		if (tank >= 0 && tank < this.tankCount) {
			fluids[tank] = stack;
			updateCallback.accept(getFluids());
		}
	}

	public SmartMultiFluidTank load(HolderLookup.Provider registries, CompoundTag nbt) {
		resetTanks();
		for (int i = 0; i < getTanks(); i++) {
			if (!nbt.contains(Integer.toString(i)))
				continue;
			CompoundTag fluidNbt = nbt.getCompoundOrEmpty(Integer.toString(i));
			FluidStack fluid = FluidStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), fluidNbt).result().orElse(FluidStack.EMPTY);
			fluids[i] = fluid;
		}
		return this;
	}

	public CompoundTag save(HolderLookup.Provider registries, CompoundTag nbt) {
		for (int i = 0; i < getTanks(); i++) {
			if (fluids[i].isEmpty()) {
				nbt.remove(Integer.toString(i));
				continue;
			}
			Tag fluidNbt = FluidStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), fluids[i]).getOrThrow();
			nbt.put(Integer.toString(i), fluidNbt);
		}
		return nbt;
	}

	@Override
	public int getTanks() {
		return tankCount;
	}

	@Override
	public @NotNull FluidStack getFluidInTank(int tank) {
		if (tank >= 0 && tank < getTanks()) {
			return fluids[tank];
		}
		return FluidStack.EMPTY;
	}

	@Override
	public int getTankCapacity(int tank) {
		return tank >= 0 && tank < tankCount ? getCapacity() : 0;
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
		return tank >= 0 && tank < tankCount && isFluidValid(stack);
	}

	OptionalInt findMatchingOrEmptyTank(FluidStack resource) {
		int firstEmpty = -1;
		for (int i = 0; i < getTanks(); i++) {
			if (fluids[i].isEmpty() && firstEmpty < 0)
				firstEmpty = i;

			if (FluidStack.isSameFluidSameComponents(fluids[i], resource))
				return OptionalInt.of(i);
		}
		if (firstEmpty >= 0)
			return OptionalInt.of(firstEmpty);
		return OptionalInt.empty();
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !isFluidValid(resource)) {
			return 0;
		}
		if (getSpace() == 0)
			return 0;

		OptionalInt targetTankOpt = findMatchingOrEmptyTank(resource);
		if (targetTankOpt.isEmpty())
			return 0;

		int filled = Math.min(getSpace(), resource.getAmount());
		if (action.simulate())
			return filled;

		int targetTank = targetTankOpt.getAsInt();
		if (fluids[targetTank].isEmpty())
			fluids[targetTank] = resource.copyWithAmount(filled);
		else
			fluids[targetTank].grow(filled);
		onContentsChanged();
		return filled;
	}

	@Override
	public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty())
			return FluidStack.EMPTY;
		OptionalInt targetTankOpt = findMatchingOrEmptyTank(resource);
		if (targetTankOpt.isEmpty())
			return FluidStack.EMPTY;
		int targetTank = targetTankOpt.getAsInt();

		if (fluids[targetTank].isEmpty())
			return FluidStack.EMPTY;

		int drained = Math.min(fluids[targetTank].getAmount(), resource.getAmount());
		FluidStack stack = fluids[targetTank].copyWithAmount(drained);

		if (action.execute() && drained > 0) {
			fluids[targetTank].shrink(drained);
			onContentsChanged();
		}
		return stack;
	}

	@Override
	public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
		if (maxDrain <= 0)
			return FluidStack.EMPTY;
		for (int i = 0; i < getTanks(); i++) {
			if (!fluids[i].isEmpty()) {
				int drained = Math.min(maxDrain, fluids[i].getAmount());
				FluidStack stack = fluids[i].copyWithAmount(drained);
				if (action.execute() && drained > 0) {
					fluids[i].shrink(drained);
					onContentsChanged();
				}
				return stack;
			}
		}
		return FluidStack.EMPTY;
	}

	@Override
	public @NotNull FluidStack getFluid() {
		for (int i = 0; i < getTanks(); i++) {
			if (!fluids[i].isEmpty())
				return fluids[i];
		}
		return FluidStack.EMPTY;
	}

	public @NotNull FluidStack[] getFluids() {
		return fluids;
	}

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public boolean isFluidValid(FluidStack stack) {
		return validator.test(stack);
	}

	public boolean isEmpty() {
		for (int i = 0; i < getTanks(); i++) {
			if (!fluids[i].isEmpty())
				return false;
		}
		return true;
	}

	public int getSpace() {
		return Math.max(0, capacity - getFluidAmount());
	}

	@Override
	public boolean canFillAll(List<FluidStack> fluids) {
		if (fluids.isEmpty()) {
			return true;
		}

		FluidStack[] simulatedFluids = new FluidStack[tankCount];
		long totalAmount = 0;
		for (int i = 0; i < tankCount; i++) {
			simulatedFluids[i] = this.fluids[i].copy();
			totalAmount += simulatedFluids[i].getAmount();
		}

		for (FluidStack fluid : fluids) {
			if (fluid.isEmpty() || !isFluidValid(fluid)) {
				return false;
			}

			if (totalAmount + fluid.getAmount() > capacity) {
				return false;
			}

			int targetTank = -1;
			for (int i = 0; i < tankCount; i++) {
				if (FluidStack.isSameFluidSameComponents(simulatedFluids[i], fluid)) {
					targetTank = i;
					break;
				}
				if (targetTank == -1 && simulatedFluids[i].isEmpty()) {
					targetTank = i;
				}
			}

			if (targetTank == -1) {
				return false;
			}

			if (simulatedFluids[targetTank].isEmpty()) {
				simulatedFluids[targetTank] = fluid.copy();
			} else {
				simulatedFluids[targetTank].grow(fluid.getAmount());
			}
			totalAmount += fluid.getAmount();
		}

		return true;
	}

    // All sided and multi-block capability views use this storage's one journal.
    // Only committed transactions invoke the existing synchronization callback.
    private final SnapshotJournal<FluidStack[]> transferJournal = new SnapshotJournal<>() {
        @Override protected FluidStack[] createSnapshot() {
            FluidStack[] copy = new FluidStack[tankCount];
            for (int i = 0; i < tankCount; i++) copy[i] = fluids[i].copy();
            return copy;
        }
        @Override protected void revertToSnapshot(FluidStack[] previous) {
            for (int i = 0; i < tankCount; i++) fluids[i] = previous[i].copy();
        }
        @Override protected void onRootCommit(FluidStack[] previous) { onContentsChanged(); }
    };

    @Override public int size() { return tankCount; }
    @Override public FluidResource getResource(int index) {
        return FluidResource.of(getFluidInTank(index));
    }
    @Override public long getAmountAsLong(int index) { return getFluidAmount(index); }
    @Override public long getCapacityAsLong(int index, FluidResource resource) {
        if (index < 0 || index >= tankCount || (!resource.isEmpty() && !isValid(index, resource))) return 0;
        return Math.min(Integer.MAX_VALUE, (long)getSpace() + getFluidAmount(index));
    }
    @Override public boolean isValid(int index, FluidResource resource) {
        if (index < 0 || index >= tankCount) return false;
        if (resource.isEmpty()) return true;
        if (!validator.test(resource.toStack(1))) return false;
        // Preserve the upstream one-storage-slot-per-fluid invariant.
        for (int i = 0; i < tankCount; i++)
            if (i != index && resource.matches(fluids[i])) return false;
        return true;
    }
    @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (amount < 0 || resource.isEmpty()) throw new IllegalArgumentException("Nonempty fluid and nonnegative amount required");
        if (amount == 0 || !isValid(index, resource)) return 0;
        FluidStack current = fluids[index];
        if (!current.isEmpty() && !resource.matches(current)) return 0;
        int accepted = Math.min(getSpace(), amount);
        if (accepted <= 0) return 0;
        transferJournal.updateSnapshots(transaction);
        fluids[index] = resource.toStack(current.getAmount() + accepted);
        return accepted;
    }
    @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (amount < 0 || resource.isEmpty()) throw new IllegalArgumentException("Nonempty fluid and nonnegative amount required");
        if (index < 0 || index >= tankCount || amount == 0 || !resource.matches(fluids[index])) return 0;
        int extracted = Math.min(amount, fluids[index].getAmount());
        if (extracted == 0) return 0;
        transferJournal.updateSnapshots(transaction);
        fluids[index] = fluids[index].copyWithAmount(fluids[index].getAmount() - extracted);
        return extracted;
    }
}
