package com.yision.fluidlogistics.foundation.capability;

import java.util.function.Function;
import com.yision.fluidlogistics.content.logistics.fluidPackage.FluidPackageContentHelper;
import com.yision.fluidlogistics.content.logistics.fluidPackage.FluidPackageFluidHandler;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandlerItem;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler.FluidAction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Mutates a private item copy, then exchanges the full item resource in the caller transaction. */
public final class ItemFluidResourceHandler implements ResourceHandler<FluidResource> {
    private final ItemAccess access;
    private final Function<ItemStack,IFluidHandlerItem> factory;
    public ItemFluidResourceHandler(ItemAccess access, Function<ItemStack,IFluidHandlerItem> factory) {
        this.access=access; this.factory=factory;
    }
    private IFluidHandlerItem view() { return factory.apply(access.getResource().toStack(1)); }
    @Override public int size() { return access.getAmount()==0 ? 0 : view().getTanks(); }
    @Override public FluidResource getResource(int index) { return FluidResource.of(view().getFluidInTank(index)); }
    @Override public long getAmountAsLong(int index) { return view().getFluidInTank(index).getAmount(); }
    @Override public long getCapacityAsLong(int index,FluidResource resource) {
        return resource.isEmpty() || isValid(index,resource) ? view().getTankCapacity(index) : 0;
    }
    @Override public boolean isValid(int index,FluidResource resource) { return !resource.isEmpty() && view().isFluidValid(index,resource.toStack(1)); }
    private boolean exchange(ItemStack result,TransactionContext transaction) {
        if(result.isEmpty()) return access.extract(access.getResource(),1,transaction)==1;
        return access.exchange(ItemResource.of(result),1,transaction)==1;
    }
    @Override public int insert(int index,FluidResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount==0 || index<0 || index>=size() || !isValid(index,resource)) return 0;
        IFluidHandlerItem copy=view();
        int accepted=copy.fill(resource.toStack(amount),FluidAction.EXECUTE);
        if(accepted==0)return 0;
        try(Transaction nested=Transaction.open(transaction)) {
            if(!exchange(copy.getContainer(),nested))return 0;
            nested.commit();return accepted;
        }
    }
    @Override public int extract(int index,FluidResource resource,int amount,TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if(amount==0 || index<0 || index>=size() || !resource.matches(view().getFluidInTank(index)))return 0;
        IFluidHandlerItem copy=view();
        // A legacy resource drain searches the first matching tank. Packages can
        // contain the same fluid in multiple slots, so preserve the requested index.
        int extracted;
        if(copy instanceof FluidPackageFluidHandler) {
            if(amount<FluidPackageContentHelper.PACKAGE_DRAIN_AMOUNT)return 0;
            extracted=FluidPackageContentHelper.drainOneBucket(copy.getContainer(),index,false).getAmount();
        } else {
            extracted=copy.drain(resource.toStack(Math.min(amount,copy.getFluidInTank(index).getAmount())),FluidAction.EXECUTE).getAmount();
        }
        if(extracted==0)return 0;
        try(Transaction nested=Transaction.open(transaction)) {
            if(!exchange(copy.getContainer(),nested))return 0;
            nested.commit();return extracted;
        }
    }
}
