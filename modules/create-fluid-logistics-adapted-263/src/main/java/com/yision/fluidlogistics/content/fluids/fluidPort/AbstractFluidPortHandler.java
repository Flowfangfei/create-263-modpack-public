package com.yision.fluidlogistics.content.fluids.fluidPort;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.yision.fluidlogistics.content.fluids.multiFluidTank.SharedCapacityFluidHandler;

import net.neoforged.neoforge.fluids.FluidStack;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;

public abstract class AbstractFluidPortHandler
	implements IFluidHandler, FluidPortHandler, SharedCapacityFluidHandler, net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> {

	private final ThreadLocal<Boolean> recursionGuard = ThreadLocal.withInitial(() -> false);

	@Nullable
	protected abstract IFluidHandler getSourceHandler();

	protected final <T> T preventRecursion(Supplier<T> value, T fallback) {
		if (recursionGuard.get()) {
			return fallback;
		}
		recursionGuard.set(true);
		try {
			return value.get();
		} finally {
			recursionGuard.set(false);
		}
	}

	@Override
	public int getTanks() {
		return preventRecursion(() -> {
			IFluidHandler handler = getSourceHandler();
			return handler == null ? 0 : handler.getTanks();
		}, 0);
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return preventRecursion(() -> {
			IFluidHandler handler = getSourceHandler();
			if (handler == null || tank < 0 || tank >= handler.getTanks()) {
				return FluidStack.EMPTY;
			}
			return handler.getFluidInTank(tank).copy();
		}, FluidStack.EMPTY);
	}

	@Override
	public int getTankCapacity(int tank) {
		return preventRecursion(() -> {
			IFluidHandler handler = getSourceHandler();
			return handler == null || tank < 0 || tank >= handler.getTanks()
				? 0
				: handler.getTankCapacity(tank);
		}, 0);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return preventRecursion(() -> {
			IFluidHandler handler = getSourceHandler();
			return handler != null && tank >= 0 && tank < handler.getTanks()
				&& handler.isFluidValid(tank, stack);
		}, false);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return preventRecursion(() -> {
			IFluidHandler handler = getSourceHandler();
			return handler == null ? 0 : handler.fill(resource, action);
		}, 0);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		return preventRecursion(() -> {
			IFluidHandler handler = getSourceHandler();
			return handler == null ? FluidStack.EMPTY : handler.drain(resource, action);
		}, FluidStack.EMPTY);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		return preventRecursion(() -> {
			IFluidHandler handler = getSourceHandler();
			return handler == null ? FluidStack.EMPTY : handler.drain(maxDrain, action);
		}, FluidStack.EMPTY);
	}

	@Override
	public boolean canFillAll(List<FluidStack> fluids) {
		return preventRecursion(() -> {
			IFluidHandler handler = getSourceHandler();
			return handler instanceof SharedCapacityFluidHandler sharedCapacityFluidHandler
				&& sharedCapacityFluidHandler.canFillAll(fluids);
		}, false);
	}

    @Override public int size(){return getTanks();}
    @Override public net.neoforged.neoforge.transfer.fluid.FluidResource getResource(int index){return net.neoforged.neoforge.transfer.fluid.FluidResource.of(getFluidInTank(index));}
    @Override public long getAmountAsLong(int index){return getFluidInTank(index).getAmount();}
    @Override public long getCapacityAsLong(int index,net.neoforged.neoforge.transfer.fluid.FluidResource resource){return resource.isEmpty()||isValid(index,resource)?getTankCapacity(index):0;}
    @Override public boolean isValid(int index,net.neoforged.neoforge.transfer.fluid.FluidResource resource){return !resource.isEmpty()&&isFluidValid(index,resource.toStack(1));}
    @Override public int insert(int index,net.neoforged.neoforge.transfer.fluid.FluidResource resource,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){
        net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount==0||!isValid(index,resource))return 0;
        return preventRecursion(()->{var handler=com.yision.fluidlogistics.foundation.capability.LegacyCapabilityAccess.resource(getSourceHandler());return handler==null?0:handler.insert(index,resource,amount,tx);},0);
    }
    @Override public int extract(int index,net.neoforged.neoforge.transfer.fluid.FluidResource resource,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){
        net.neoforged.neoforge.transfer.TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount==0||!resource.matches(getFluidInTank(index)))return 0;
        return preventRecursion(()->{var handler=com.yision.fluidlogistics.foundation.capability.LegacyCapabilityAccess.resource(getSourceHandler());return handler==null?0:handler.extract(index,resource,amount,tx);},0);
    }
}
