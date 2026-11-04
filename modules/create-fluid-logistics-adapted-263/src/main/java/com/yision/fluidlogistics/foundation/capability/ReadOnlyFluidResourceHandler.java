package com.yision.fluidlogistics.foundation.capability;
import java.util.function.Supplier;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
/** The package-entity/compressed-item read-only contract, with live content inspection. */
public record ReadOnlyFluidResourceHandler(Supplier<IFluidHandler> source) implements ResourceHandler<FluidResource> {
 public int size(){return source.get().getTanks();}
 public FluidResource getResource(int i){return FluidResource.of(source.get().getFluidInTank(i));}
 public long getAmountAsLong(int i){return source.get().getFluidInTank(i).getAmount();}
 public long getCapacityAsLong(int i,FluidResource r){return r.isEmpty()?source.get().getTankCapacity(i):0;}
 public boolean isValid(int i,FluidResource r){return false;}
 public int insert(int i,FluidResource r,int a,TransactionContext tx){return 0;}
 public int extract(int i,FluidResource r,int a,TransactionContext tx){return 0;}
}
