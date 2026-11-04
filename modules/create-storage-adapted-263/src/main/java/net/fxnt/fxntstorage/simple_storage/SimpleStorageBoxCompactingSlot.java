package net.fxnt.fxntstorage.simple_storage;

import net.fxnt.fxntstorage.init.ModItems;
import net.minecraft.world.item.ItemStack;
import com.simibubi.create.compat.neoforge263.items.IItemHandler;
import com.simibubi.create.compat.neoforge263.items.SlotItemHandler;

public class SimpleStorageBoxCompactingSlot extends SlotItemHandler {

    public SimpleStorageBoxCompactingSlot(IItemHandler itemHandler, int slot, int x, int y) {
        super(itemHandler, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (this.hasItem()) return false;
        return stack.is(ModItems.STORAGE_BOX_COMPACTING_UPGRADE.get());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }
}
