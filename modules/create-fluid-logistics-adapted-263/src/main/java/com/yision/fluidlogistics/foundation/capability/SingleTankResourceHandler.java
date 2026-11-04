package com.yision.fluidlogistics.foundation.capability;

import java.util.function.BooleanSupplier;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Keeps the owner's validation and infinite-source rules, without notifying on aborted transactions. */
public final class SingleTankResourceHandler implements ResourceHandler<FluidResource> {
 private final SmartFluidTank tank;
 private final BooleanSupplier infinite;
 private final SnapshotJournal<FluidStack> journal=new SnapshotJournal<>() {
  protected FluidStack createSnapshot(){return tank.getFluid().copy();}
  protected void revertToSnapshot(FluidStack snapshot){tank.setFluidSilently(snapshot);}
  protected void onRootCommit(FluidStack snapshot){tank.setFluid(tank.getFluid());}
 };
 public SingleTankResourceHandler(SmartFluidTank tank){this(tank,()->false);}
 public SingleTankResourceHandler(SmartFluidTank tank,BooleanSupplier infinite){this.tank=tank;this.infinite=infinite;}
 public int size(){return 1;}
 public FluidResource getResource(int index){return FluidResource.of(tank.getFluid());}
 public long getAmountAsLong(int index){return tank.getFluidAmount();}
 public long getCapacityAsLong(int index,FluidResource resource){return resource.isEmpty()||isValid(index,resource)?tank.getCapacity():0;}
 public boolean isValid(int index,FluidResource resource){return !resource.isEmpty()&&tank.isFluidValid(resource.toStack(1));}
 public int insert(int index,FluidResource resource,int amount,TransactionContext tx){
  TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
  if(index!=0||amount==0)return 0;
  int accepted=tank.fill(resource.toStack(amount),FluidAction.SIMULATE);
  if(accepted==0||infinite.getAsBoolean())return accepted;
  journal.updateSnapshots(tx);
  int total=(int)Math.min(tank.getCapacity(),(long)tank.getFluidAmount()+accepted);
  tank.setFluidSilently(resource.toStack(total));return accepted;
 }
 public int extract(int index,FluidResource resource,int amount,TransactionContext tx){
  TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
  if(index!=0||amount==0)return 0;
  FluidStack extracted=tank.drain(resource.toStack(amount),FluidAction.SIMULATE);
  if(extracted.isEmpty())return 0;
  if(!infinite.getAsBoolean()){
   journal.updateSnapshots(tx);
   int left=tank.getFluidAmount()-extracted.getAmount();
   tank.setFluidSilently(left==0?FluidStack.EMPTY:tank.getFluid().copyWithAmount(left));
  }
  return extracted.getAmount();
 }
}
