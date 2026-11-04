/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.psi;

import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.fluid.ResourceHandlerFluidAdapter.TransferCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler.FluidAction;
import com.simibubi.create.compat.neoforge263.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PortableFuelInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

	protected IFluidHandler capability;
	private ResourceHandler<FluidResource> fluidResourceCapability;
	private boolean resourceTransaction;

	public PortableFuelInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		capability = createEmptyHandler();
	}


	public ResourceHandler<FluidResource> getFluidResourceHandler() {
		if (fluidResourceCapability == null)
			fluidResourceCapability = new InterfaceFluidResourceHandler();
		return fluidResourceCapability;
	}

	@Override
	public void startTransferringTo(Contraption contraption, float distance) {
		var fuelStorage = ((IFuelInventory) contraption.getStorage()).railways$getFluidFuels();
		capability = new InterfaceFluidHandler(fuelStorage == null ? new FluidTank(0) : fuelStorage);
		invalidateCapability();
		if (level != null && !level.isClientSide())
			level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
		super.startTransferringTo(contraption, distance);
	}

	@Override
	protected void invalidateCapability() {
		fluidResourceCapability = null;
		invalidateCapabilities();
	}

	@Override
	protected void stopTransferring() {
		capability = createEmptyHandler();
		invalidateCapability();
		if (level != null && !level.isClientSide())
			level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
		super.stopTransferring();
	}

	private IFluidHandler createEmptyHandler() {
		return new InterfaceFluidHandler(new FluidTank(0));
	}

    public boolean isConnected() {
        return transferTimer >= ANIMATION && transferTimer <= getTransferTimeout() + ANIMATION;
    }

    public float getExtensionDistance(float partialTicks) {
        return (float) (Math.pow(connectionAnimation.getValue(partialTicks), 2) * distance / 2);
    }

    public float getConnectionDistance() {
        return distance;
    }

	public class InterfaceFluidHandler implements IFluidHandler {

		private IFluidHandler wrapped;

		public InterfaceFluidHandler(IFluidHandler wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public int getTanks() {
			return wrapped.getTanks();
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return wrapped.getFluidInTank(tank);
		}

		@Override
		public int getTankCapacity(int tank) {
			return wrapped.getTankCapacity(tank);
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return wrapped.isFluidValid(tank, stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (!isConnected())
				return 0;
			int fill = wrapped.fill(resource, action);
			if (fill > 0 && action.execute() && !resourceTransaction)
				keepAlive();
			return fill;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if (!canTransfer())
				return FluidStack.EMPTY;
			FluidStack drain = wrapped.drain(resource, action);
			if (!drain.isEmpty() && action.execute() && !resourceTransaction)
				keepAlive();
			return drain;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if (!canTransfer())
				return FluidStack.EMPTY;
			FluidStack drain = wrapped.drain(maxDrain, action);
			if (!drain.isEmpty() && action.execute() && !resourceTransaction)
				keepAlive();
			return drain;
		}

		public void keepAlive() {
			onContentTransferred();
		}

	}

	private class InterfaceFluidResourceHandler implements ResourceHandler<FluidResource>, TransferCallback {
		private final FluidJournal journal = new FluidJournal();

		@Override
		public void onTransferCommitted() {
			onContentTransferred();
		}

		@Override
		public int size() {
			return capability.getTanks();
		}

		@Override
		public FluidResource getResource(int index) {
			return FluidResource.of(capability.getFluidInTank(index));
		}

		@Override
		public long getAmountAsLong(int index) {
			return capability.getFluidInTank(index)
				.getAmount();
		}

		@Override
		public long getCapacityAsLong(int index, FluidResource resource) {
			if (!resource.isEmpty() && !isValid(index, resource))
				return 0;
			return capability.getTankCapacity(index);
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			if (resource.isEmpty())
				return true;
			return capability.isFluidValid(index, resource.toStack(1));
		}

		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			if (resource.isEmpty() || amount <= 0)
				return 0;

			FluidStack stack = resource.toStack(amount);
			int inserted = capability.fill(stack, FluidAction.SIMULATE);
			if (inserted <= 0)
				return 0;

			journal.updateSnapshots(transaction);
			resourceTransaction = true;
			try {
				return capability.fill(resource.toStack(inserted), FluidAction.EXECUTE);
			} finally {
				resourceTransaction = false;
			}
		}

		@Override
		public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
			if (resource.isEmpty() || amount <= 0)
				return 0;

			FluidStack extracted = capability.drain(resource.toStack(amount), FluidAction.SIMULATE);
			if (extracted.isEmpty())
				return 0;

			journal.updateSnapshots(transaction);
			resourceTransaction = true;
			try {
				return capability.drain(extracted, FluidAction.EXECUTE)
					.getAmount();
			} finally {
				resourceTransaction = false;
			}
		}

		private NonNullList<FluidStack> createSnapshot() {
			NonNullList<FluidStack> snapshot = NonNullList.create();
			for (int i = 0; i < capability.getTanks(); i++)
				snapshot.add(capability.getFluidInTank(i)
					.copy());
			return snapshot;
		}

		private void restoreSnapshot(NonNullList<FluidStack> snapshot) {
			resourceTransaction = true;
			try {
				for (int i = 0; i < capability.getTanks(); i++)
					capability.drain(capability.getFluidInTank(i), FluidAction.EXECUTE);
				for (FluidStack stack : snapshot)
					if (!stack.isEmpty())
						capability.fill(stack.copy(), FluidAction.EXECUTE);
			} finally {
				resourceTransaction = false;
			}
		}

		private class FluidJournal extends SnapshotJournal<NonNullList<FluidStack>> {
			@Override
			protected NonNullList<FluidStack> createSnapshot() {
				return InterfaceFluidResourceHandler.this.createSnapshot();
			}

			@Override
			protected void revertToSnapshot(NonNullList<FluidStack> snapshot) {
				restoreSnapshot(snapshot);
			}

			@Override
			protected void onRootCommit(NonNullList<FluidStack> snapshot) {
				onContentTransferred();
			}
		}
	}

}
