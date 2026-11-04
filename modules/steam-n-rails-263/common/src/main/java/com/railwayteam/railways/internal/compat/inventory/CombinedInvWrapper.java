package com.railwayteam.railways.internal.compat.inventory;

import com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable;
import com.simibubi.create.compat.neoforge263.items.wrapper.InvWrapper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import java.util.function.Predicate;

/** Railway's exact-count operations over the real Create mounted-item inventory. */
public class CombinedInvWrapper extends com.simibubi.create.compat.neoforge263.items.wrapper.CombinedInvWrapper {
 public CombinedInvWrapper(IItemHandlerModifiable... handlers) { super(handlers); }
 public CombinedInvWrapper(Container container) { super(new InvWrapper(container)); }
 public ItemStack count(Predicate<ItemStack> predicate,int max) {
  for(int i=0;i<getSlots();i++) { var stack=getStackInSlot(i);if(!stack.isEmpty() && predicate.test(stack))return stack.copyWithCount(Math.min(max,stack.getCount())); }
  return ItemStack.EMPTY;
 }
 public ItemStack preciseCount(Predicate<ItemStack> predicate,int amount) {
  for(int i=0;i<getSlots();i++) {
   var candidate=getStackInSlot(i);if(candidate.isEmpty() || !predicate.test(candidate))continue;
   int count=0;
   for(int j=0;j<getSlots() && count<amount;j++)if(ItemStack.isSameItemSameComponents(candidate,getStackInSlot(j)))count+=extractItem(j,amount-count,true).getCount();
   if(count>=amount)return candidate.copyWithCount(amount);
  }
  return ItemStack.EMPTY;
 }
 public ItemStack extract(Predicate<ItemStack> predicate,int max) { return extract(predicate,max,null); }
 public ItemStack extract(Predicate<ItemStack> predicate,int max,Direction side) {
  var selected=count(predicate,1);if(selected.isEmpty())return ItemStack.EMPTY;
  int extracted=0;
  for(int i=0;i<getSlots() && extracted<max;i++)if(ItemStack.isSameItemSameComponents(selected,getStackInSlot(i)))extracted+=extractItem(i,max-extracted,false).getCount();
  return extracted==0?ItemStack.EMPTY:selected.copyWithCount(extracted);
 }
 public int insert(ItemStack stack,int amount) {
  var remainder=stack.copyWithCount(amount);
  for(int i=0;i<getSlots() && !remainder.isEmpty();i++)remainder=insertItem(i,remainder,false);
  return amount-remainder.getCount();
 }
}
