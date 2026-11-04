package dev.simulated_team.simulated.multiloader.inventory.neoforge;

import dev.simulated_team.simulated.multiloader.inventory.AbstractContainer;
import net.minecraft.world.item.ItemStack;
import com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class ContainerWrapper<T extends AbstractContainer> implements IItemHandlerModifiable, net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> {

    private final  T container;

    public ContainerWrapper(final T container) {
        this.container = container;
    }

    @Override
    public @NotNull ItemStack insertItem(final int slot, @NotNull final ItemStack stack, final boolean simulate) {
        return this.container.insertSlot(stack, slot, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(final int slot, final int maxSize, final boolean simulate) {
        return this.container.extractSlot(slot, maxSize, simulate);
    }

    @Override
    public void setStackInSlot(final int i, @NotNull final ItemStack arg) {
        this.container.setItem(i, arg);
    }

    @Override
    public int getSlots() {
        return this.container.getContainerSize();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(final int i) {
        return this.container.getItem(i);
    }

    @Override
    public int getSlotLimit(final int i) {
        return this.container.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(final int i, @NotNull final ItemStack arg) {
        validate(i);
        return this.container.canInsertItem(dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper.generateFromStack(arg),this.container.getInventoryAsList().get(i));
    }
    private void validate(int slot){if(slot<0||slot>=getSlots())throw new IndexOutOfBoundsException(slot);}
    private void snapshot(int slot,net.neoforged.neoforge.transfer.transaction.TransactionContext transaction){
        AbstractContainer backend=container.getInventoryAsList().get(slot).getParent();
        dev.simulated_team.simulated.multiloader.transfer.BackendTransactions.snapshot(backend,transaction,
            ()->{java.util.List<ItemStack> state=new java.util.ArrayList<>();for(var entry:backend.getInventoryAsList())state.add(entry.getStack().copy());return state;},
            state->{var slots=backend.getInventoryAsList();for(int i=0;i<state.size();i++)slots.get(i).setStackSilently(state.get(i).copy());},
            state->{var slots=backend.getInventoryAsList();for(int i=0;i<state.size();i++){
                if(!ItemStack.matches(state.get(i),slots.get(i).getStack()))slots.get(i).notifyCommitted(state.get(i));
            }backend.setChanged();});
    }
    @Override public int size(){return getSlots();}
    @Override public net.neoforged.neoforge.transfer.item.ItemResource getResource(int slot){validate(slot);return net.neoforged.neoforge.transfer.item.ItemResource.of(getStackInSlot(slot));}
    @Override public long getAmountAsLong(int slot){validate(slot);return getStackInSlot(slot).getCount();}
    @Override public long getCapacityAsLong(int slot,net.neoforged.neoforge.transfer.item.ItemResource resource){validate(slot);return Math.min(getSlotLimit(slot),resource.isEmpty()?getSlotLimit(slot):resource.getMaxStackSize());}
    @Override public boolean isValid(int slot,net.neoforged.neoforge.transfer.item.ItemResource resource){validate(slot);return !resource.isEmpty()&&isItemValid(slot,resource.toStack());}
    @Override public int insert(int slot,net.neoforged.neoforge.transfer.item.ItemResource resource,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext transaction){
        validate(slot);if(amount<0)throw new IllegalArgumentException("Negative amount");
        if(resource.isEmpty())throw new IllegalArgumentException("Empty resource");
        if(amount==0||!isValid(slot,resource)||(!getStackInSlot(slot).isEmpty()&&!resource.matches(getStackInSlot(slot))))return 0;
        ItemStack stack=resource.toStack(amount);int accepted=amount-insertItem(slot,stack,true).getCount();
        if(accepted<=0)return 0;
        snapshot(slot,transaction);
        ItemStack next=getStackInSlot(slot).isEmpty()?resource.toStack(accepted):getStackInSlot(slot).copyWithCount(getStackInSlot(slot).getCount()+accepted);
        container.getInventoryAsList().get(slot).setStackSilently(next);return accepted;
    }
    @Override public int extract(int slot,net.neoforged.neoforge.transfer.item.ItemResource resource,int amount,net.neoforged.neoforge.transfer.transaction.TransactionContext transaction){
        validate(slot);if(amount<0)throw new IllegalArgumentException("Negative amount");
        if(resource.isEmpty())throw new IllegalArgumentException("Empty resource");
        if(amount==0||!resource.matches(getStackInSlot(slot)))return 0;
        int accepted=extractItem(slot,amount,true).getCount();
        if(accepted<=0)return 0;
        snapshot(slot,transaction);
        container.getInventoryAsList().get(slot).setStackSilently(getStackInSlot(slot).copyWithCount(getStackInSlot(slot).getCount()-accepted));return accepted;
    }
}
