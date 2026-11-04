package net.fxnt.fxntstorage.inventory;

import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.BackpackBlock;
import net.fxnt.fxntstorage.backpack.inventory.BackpackSlotLayout;
import net.fxnt.fxntstorage.init.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Component exchange is journaled by the caller's native ItemAccess, never by a detached copy. */
public final class StorageBackpackTransfer implements ResourceHandler<ItemResource> {
    private final ItemAccess access;
    private StorageContents validatedContents;
    private int validatedMultiplier;
    private static final BackpackSlotLayout LAYOUT=BackpackSlotLayout.createLayout();
    public StorageBackpackTransfer(ItemAccess access){this.access=access;}
    private ItemStack backpack(){return access.getResource().toStack(1);}
    private StorageContents contents(){
        ItemStack stack=backpack();
        StorageContents current=stack.getOrDefault(ModDataComponents.STORAGE_CONTENTS,StorageContents.EMPTY);
        int multiplier=((BackpackBlock)((BackpackItem)stack.getItem()).getBlock()).getStackMultiplier();
        if(current!=validatedContents||multiplier!=validatedMultiplier){
            BackpackInventoryRules.validateContents(current,multiplier);
            validatedContents=current;validatedMultiplier=multiplier;
        }
        return current;
    }
    @Override public int size(){return access.getAmount()==1&&backpack().getItem() instanceof BackpackItem?LAYOUT.items().getEndIndex():0;}
    private void check(int index){java.util.Objects.checkIndex(index,size());}
    @Override public ItemResource getResource(int index){check(index);return ItemResource.of(contents().getStackInSlot(index));}
    @Override public long getAmountAsLong(int index){check(index);return contents().getStackInSlot(index).getCount();}
    @Override public boolean isValid(int index,ItemResource resource){
        check(index);return !resource.isEmpty()&&!(resource.toStack(1).getItem() instanceof BackpackItem);
    }
    @Override public long getCapacityAsLong(int index,ItemResource resource){
        check(index);if(!resource.isEmpty()&&!isValid(index,resource))return 0;
        BackpackBlock block=(BackpackBlock)((BackpackItem)backpack().getItem()).getBlock();
        return (long)(resource.isEmpty()?64:resource.getMaxStackSize())*block.getStackMultiplier();
    }
    private boolean replace(int index,ItemStack stack,TransactionContext transaction){
        ItemStack result=backpack();
        result.set(ModDataComponents.STORAGE_CONTENTS,contents().withSlot(index,stack,LAYOUT.getTotalSlots()));
        try(Transaction nested=Transaction.open(transaction)){
            if(access.exchange(ItemResource.of(result),1,nested)!=1)return false;
            nested.commit();return true;
        }
    }
    @Override public int insert(int index,ItemResource resource,int amount,TransactionContext transaction){
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);check(index);
        if(amount==0||!isValid(index,resource))return 0;
        ItemStack previous=contents().getStackInSlot(index);
        if(!previous.isEmpty()&&!resource.matches(previous))return 0;
        int accepted=(int)Math.min(amount,Math.max(0,getCapacityAsLong(index,resource)-previous.getCount()));
        return accepted>0&&replace(index,resource.toStack(previous.getCount()+accepted),transaction)?accepted:0;
    }
    @Override public int extract(int index,ItemResource resource,int amount,TransactionContext transaction){
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);check(index);
        ItemStack previous=contents().getStackInSlot(index);
        if(amount==0||!resource.matches(previous))return 0;
        int taken=Math.min(amount,previous.getCount());
        return taken>0&&replace(index,previous.copyWithCount(previous.getCount()-taken),transaction)?taken:0;
    }
}
