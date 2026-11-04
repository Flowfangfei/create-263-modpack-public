package dev.simulated_team.simulated.multiloader.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ContainerSlot implements NBTSerializable, SlotAccess {

    public static final ContainerSlot EMPTY = new ContainerSlot();

    private final int index;
    private final AbstractContainer parent;

    private Item type;
    private ItemStack stack;

    public ContainerSlot() {
        this.index = -1;
        this.parent = null;

        this.type = ItemStack.EMPTY.getItem();
        this.stack = ItemStack.EMPTY;
    }

    public ContainerSlot(final int slot, final ItemStack stack, final Item type, final AbstractContainer parent) {
        this.index = slot;
        this.stack = stack;
        this.type = type;
        this.parent = parent;

        //populate the parent's fields
        parent.populateFields(this);
    }

    public static ContainerSlot of(final int slot, final Item type, final AbstractContainer parent) {
        return of(slot, new ItemStack(type), parent);
    }

    public static ContainerSlot of(final int slot, final ItemStack stack, final AbstractContainer parent) {
        return new ContainerSlot(slot, stack, stack.getItem(), parent);
    }

    public int insertStack(final ItemInfoWrapper info, final int maxAmount, final boolean simulate) {
        //Make sure the incoming item is valid to be inserted into this slot, and the held item is either empty or the same item
        final ItemStack requested = ItemInfoWrapper.generateFromInfo(info);
        if (maxAmount > 0 && this.canInsert(info) && (this.getStack().isEmpty() || ItemStack.isSameItemSameComponents(this.getStack(), requested))) {
            //Add max stack size limitation
            final int insertedAmount = Math.max(0, Math.min(Math.min(this.parent.getMaxStackSize(), requested.getMaxStackSize()) - this.getStack().getCount(), maxAmount));

            //set the current stack in this slot if this isn't a simulated action
            if (!simulate && insertedAmount > 0) {
                ItemStack newstack = this.getStack().copy();
                if (newstack.isEmpty()) {
                    newstack = ItemInfoWrapper.generateFromInfo(info);

                    newstack.setCount(insertedAmount);
                } else {
                    newstack.grow(insertedAmount);
                }

                this.setStack(newstack);
            }

            return insertedAmount;
        }

        return 0;
    }

    public int extractStack(final ItemInfoWrapper info, final int maxAmount, final boolean simulate) {
        if (this.canExtract() && !this.getStack().isEmpty()) {
            if (maxAmount <= 0 || (info != null && !ItemStack.isSameItemSameComponents(this.getStack(), ItemInfoWrapper.generateFromInfo(info)))) {
                return 0;
            }

            final int extracted = Math.min(this.getStack().getCount(), maxAmount);
            if (!simulate && extracted > 0) {
                final ItemStack newStack = this.getStack().copy();
                newStack.shrink(extracted);

                this.setStack(newStack);
            }

            return extracted;
        }

        return 0;
    }

    public void shrink(final long amountToShrink) {
        ItemStack copied = this.getStack().copy();

        if (copied.getCount() - amountToShrink <= 0) {
            copied = ItemStack.EMPTY;
        } else {
            copied.shrink((int) amountToShrink);
        }

        this.setStack(copied);
    }

    public boolean canInsert(final ItemInfoWrapper info) {
        return this.parent.canInsertItem(info, this);
    }

    public boolean canExtract() {
        return this.parent.canExtractFromSlot(this);
    }

    public boolean isEmpty() {
        return this == EMPTY || this.getStack().isEmpty();
    }

    public void setStack(final ItemStack stack) {
        final ItemStack previous = this.stack;
        this.setStackSilently(stack);
        this.notifyCommitted(previous);
        this.parent.setChanged();
    }

    /** Update only contents and derived indexes; no weapon, network or dirty callbacks. */
    public void setStackSilently(final ItemStack stack) {
        if (this.parent == null) throw new IllegalStateException("Cannot write a sentinel slot");
        this.parent.onStackContentsChanged(this, this.stack, stack);
        this.stack = stack;
        this.type = stack.getItem();
    }

    public void notifyCommitted(final ItemStack previous) {
        this.parent.onStackItemChange(this, previous, this.stack);
    }

    public int getIndex() {
        return this.index;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public ItemStack get() {
        return getStack();
    }

    @Override
    public boolean set(final ItemStack stack) {
        if (this.parent == null) {
            return false;
        }
        setStack(stack);
        return true;
    }

    public Item getType() {
        return this.type;
    }

    public AbstractContainer getParent() {
        return this.parent;
    }

    public void clear() {
        this.setStack(ItemStack.EMPTY);
    }

    @Override
    public CompoundTag write(final HolderLookup.Provider provider) {
        final CompoundTag slotTag = new CompoundTag();

        slotTag.putInt("index", this.getIndex());
        if (!this.getStack().isEmpty()) {
            slotTag.put("item", ItemStack.CODEC.encodeStart(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), this.getStack()).getOrThrow());
        }

        return slotTag;
    }

    @Override
    public void read(final HolderLookup.Provider provider, final CompoundTag nbt) {
        ItemStack restored = nbt.contains("item")
                ? ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), nbt.getCompoundOrEmpty("item")).getOrThrow()
                : ItemStack.EMPTY;
        this.setStackSilently(restored);
    }
}
