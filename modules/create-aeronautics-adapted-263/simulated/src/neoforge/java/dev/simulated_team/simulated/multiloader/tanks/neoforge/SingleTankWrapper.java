package dev.simulated_team.simulated.multiloader.tanks.neoforge;

import dev.simulated_team.simulated.multiloader.tanks.CFluidType;
import dev.simulated_team.simulated.multiloader.tanks.SingleTank;
import net.neoforged.neoforge.fluids.FluidStack;
import com.simibubi.create.compat.neoforge263.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class SingleTankWrapper extends FluidTank implements net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> {
    private final SingleTank tank;

    public SingleTankWrapper(final SingleTank tank) {
        super((int) tank.capacity);
        this.tank = tank;
    }

    public static FluidStack fromCType(final CFluidType type, final int amount) {
        if(type.isBlank() || amount<=0)return FluidStack.EMPTY;
        var stack = new FluidStack(type.fluid, amount);
        stack.applyComponents(type.data());
        var removals=net.minecraft.core.component.DataComponentPatch.builder();
        for(var key:stack.getPrototype().keySet())if(!type.data().has(key))removals.remove(key);
        stack.applyComponents(removals.build());
        return stack;
    }

    public static CFluidType toCType(final FluidStack stack) {
        return new CFluidType(stack.getFluid(), stack.getComponents());
    }

    @Override
    public int fill(final FluidStack resource, final FluidAction action) {
        return (int) this.tank.insert(toCType(resource), resource.getAmount(), action.simulate());
    }

    @Override
    public @NotNull FluidStack drain(final int maxDrain, final FluidAction action) {
        return fromCType(this.tank.type, (int) this.tank.extract(this.tank.type, maxDrain, action.simulate()));
    }

    @Override
    public @NotNull FluidStack getFluid() {
        return fromCType(this.tank.type, (int) this.tank.amount);
    }

    @Override public FluidStack drain(FluidStack requested,FluidAction action){
        if(requested.isEmpty()||!toCType(requested).equals(tank.type))return FluidStack.EMPTY;
        return drain(requested.getAmount(),action);
    }
    @Override public int getFluidAmount(){return (int)tank.amount;}
    @Override public FluidStack getFluidInTank(int index){if(index!=0)throw new IndexOutOfBoundsException(index);return getFluid();}
    private static void snapshot(SingleTank target, net.neoforged.neoforge.transfer.transaction.TransactionContext transaction) {
        dev.simulated_team.simulated.multiloader.transfer.BackendTransactions.snapshot(target, transaction,
            () -> com.mojang.datafixers.util.Pair.of(target.type, target.amount),
            state -> { target.type = state.getFirst(); target.amount = state.getSecond(); }, target::onContentsChanged);
    }
    private void validate(int index){if(index!=0)throw new IndexOutOfBoundsException(index);}
    @Override public int size(){return 1;}
    @Override public net.neoforged.neoforge.transfer.fluid.FluidResource getResource(int index){validate(index);return net.neoforged.neoforge.transfer.fluid.FluidResource.of(getFluid());}
    @Override public long getAmountAsLong(int index){validate(index);return tank.amount;}
    @Override public long getCapacityAsLong(int index,net.neoforged.neoforge.transfer.fluid.FluidResource resource){validate(index);return tank.capacity;}
    @Override public boolean isValid(int index,net.neoforged.neoforge.transfer.fluid.FluidResource resource){validate(index);return !resource.isEmpty()&&isFluidValid(resource.toStack(1));}
    @Override public int insert(int index,net.neoforged.neoforge.transfer.fluid.FluidResource resource,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext transaction){
        validate(index); if(amount<0)throw new IllegalArgumentException("Negative amount");
        if(resource.isEmpty())throw new IllegalArgumentException("Empty resource");
        if(amount==0||!isValid(index,resource))return 0;
        SingleTank target=tank.insertionTarget(); if(target==null)return 0;
        CFluidType type=toCType(resource.toStack(1));
        long accepted=SingleTank.calculateInsert(target,type,amount);
        if(accepted<=0)return 0;
        snapshot(target,transaction); SingleTank.applyInsert(target,type,accepted); return (int)accepted;
    }
    @Override public int extract(int index,net.neoforged.neoforge.transfer.fluid.FluidResource resource,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext transaction){
        validate(index); if(amount<0)throw new IllegalArgumentException("Negative amount");
        if(resource.isEmpty())throw new IllegalArgumentException("Empty resource");
        if(amount==0)return 0;
        long accepted=SingleTank.calculateExtract(tank,toCType(resource.toStack(1)),amount);
        if(accepted<=0)return 0;
        snapshot(tank,transaction); SingleTank.applyExtract(tank,accepted); return (int)accepted;
    }
}
