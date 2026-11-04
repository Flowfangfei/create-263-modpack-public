package com.yision.fluidlogistics.foundation.capability;
import java.util.function.Supplier;
import java.util.function.Predicate;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
/** Delegates transactions to one shared owner journal while retaining side/filter rules. */
public record FilteredFluidResourceHandler(Supplier<ResourceHandler<FluidResource>> source, Predicate<FluidResource> insertAllowed, Predicate<FluidResource> extractAllowed) implements ResourceHandler<FluidResource> {
 public int size(){return source.get()==null?0:source.get().size();}
 public FluidResource getResource(int i){return source.get().getResource(i);}
 public long getAmountAsLong(int i){return source.get().getAmountAsLong(i);}
 public long getCapacityAsLong(int i,FluidResource r){return r.isEmpty()||isValid(i,r)?source.get().getCapacityAsLong(i,r):0;}
 public boolean isValid(int i,FluidResource r){return source.get()!=null&&insertAllowed.test(r)&&source.get().isValid(i,r);}
 public int insert(int i,FluidResource r,int a,TransactionContext tx){return source.get()!=null&&insertAllowed.test(r)?source.get().insert(i,r,a,tx):0;}
 public int extract(int i,FluidResource r,int a,TransactionContext tx){return source.get()!=null&&extractAllowed.test(r)?source.get().extract(i,r,a,tx):0;}
}
