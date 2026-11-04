package net.fxnt.fxntstorage.inventory;

import com.simibubi.create.compat.neoforge263.items.ItemStackHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Only Storage-owned handlers use this persistence format. */
public class StorageInventoryHandler extends ItemStackHandler {
    public StorageInventoryHandler() { this(1); }
    public StorageInventoryHandler(int slots) { super(slots); }

    /** Override for slot semantics such as the fixed backpack layout. This method never mutates storage. */
    public void validateContents(StorageContents contents) {
        if (contents.getSlots() < 0 || contents.getSlots() > StorageContents.MAX_SLOTS)
            throw new IllegalArgumentException("Invalid private Storage inventory size");
        for (int slot = 0; slot < contents.getSlots(); slot++)
            validateStackInSlot(slot, contents.getStackInSlot(slot));
    }

    /** Box inventories retain independent large counts and ordinary nested container components. */
    protected void validateStackInSlot(int slot, ItemStack stack) {
        java.util.Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) return;
        if (stack.getCount() < 1 || stack.getCount() > StorageContents.MAX_COUNT)
            throw new IllegalArgumentException("Invalid private Storage item quantity");
        var validation = ItemStack.validateStrict(stack.copyWithCount(1));
        if (validation.isError())
            throw new IllegalArgumentException("Invalid stored item components: " + validation.error().orElseThrow().message());
    }

    protected int restoredSlotCount(StorageContents contents) { return contents.getSlots(); }

    /** Decode and validate first, stage a complete replacement, then swap the live list once. */
    public void replaceContents(StorageContents contents) {
        java.util.Objects.requireNonNull(contents, "contents");
        validateContents(contents);
        int size = restoredSlotCount(contents);
        if (size < contents.getSlots() || size < 0 || size > StorageContents.MAX_SLOTS)
            throw new IllegalArgumentException("Private Storage inventory does not fit its destination");
        NonNullList<ItemStack> replacement = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int slot = 0; slot < size; slot++) {
            ItemStack value = contents.getStackInSlot(slot);
            validateStackInSlot(slot, value);
            replacement.set(slot, value);
        }
        this.stacks = replacement;
        onLoad();
        for (int slot = 0; slot < size; slot++) onContentsChanged(slot);
    }

    @Override public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        validateStackInSlot(slot, stack);
        super.setStackInSlot(slot, stack);
    }

    @Override public void serialize(ValueOutput output) {
        StorageContents contents = StorageContents.fromHandler(this);
        validateContents(contents);
        output.store(StorageContents.MAP_CODEC, contents);
    }

    @Override public void deserialize(ValueInput input) {
        StorageContents contents = input.read(StorageContents.MAP_CODEC)
                .orElseThrow(() -> new IllegalArgumentException("Invalid private Storage inventory"));
        replaceContents(contents);
    }
}
