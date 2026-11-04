package com.yision.fluidlogistics.content.fluids.waterContainingCopperCasing;

import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.fluids.FluidStack;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;

public enum WaterContainingCopperCasingFluidHandler implements IFluidHandler, net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> {
	INSTANCE;

	private static FluidStack water() {
        return new FluidStack(Fluids.WATER, Integer.MAX_VALUE);
    }

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return tank == 0 ? water() : FluidStack.EMPTY;
	}

	@Override
	public int getTankCapacity(int tank) {
		return tank == 0 ? Integer.MAX_VALUE : 0;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return tank == 0 && !stack.isEmpty();
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty()) {
			return 0;
		}
		return resource.getAmount();
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, water())) {
			return FluidStack.EMPTY;
		}
		return resource.copy();
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (maxDrain <= 0) {
			return FluidStack.EMPTY;
		}
		return new FluidStack(Fluids.WATER, maxDrain);
	}

 public int size(){return 1;}
 public net.neoforged.neoforge.transfer.fluid.FluidResource getResource(int i){return net.neoforged.neoforge.transfer.fluid.FluidResource.of(getFluidInTank(i));}
 public long getAmountAsLong(int i){return getFluidInTank(i).getAmount();}
 public long getCapacityAsLong(int i,net.neoforged.neoforge.transfer.fluid.FluidResource r){return getTankCapacity(i);}
 public boolean isValid(int i,net.neoforged.neoforge.transfer.fluid.FluidResource r){return i==0&&!r.isEmpty();}
 public int insert(int i,net.neoforged.neoforge.transfer.fluid.FluidResource r,int a,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(r,a);return i==0?a:0;}
 public int extract(int i,net.neoforged.neoforge.transfer.fluid.FluidResource r,int a,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(r,a);return i==0?drain(r.toStack(a),FluidAction.SIMULATE).getAmount():0;}
}
