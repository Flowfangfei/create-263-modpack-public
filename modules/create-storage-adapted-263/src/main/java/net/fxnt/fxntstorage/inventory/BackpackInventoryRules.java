package net.fxnt.fxntstorage.inventory;

import java.util.Objects;
import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.inventory.BackpackSlotLayout;
import net.fxnt.fxntstorage.backpack.upgrade.UpgradeType;
import net.fxnt.fxntstorage.backpack.upgrade.jetpack.JetpackModifier;
import net.fxnt.fxntstorage.backpack.upgrade.workshop.WorkshopRecipeHelper;
import net.fxnt.fxntstorage.backpack.upgrade.workshop.WorkshopUpgrade;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/** Slot rules for Storage backpacks only; ordinary container items retain their own components. */
public final class BackpackInventoryRules {
    private static final BackpackSlotLayout LAYOUT = BackpackSlotLayout.createLayout();
    public static final int SLOT_COUNT = LAYOUT.getTotalSlots();

    private BackpackInventoryRules() {}

    private static void checkSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) throw new IllegalArgumentException("Invalid backpack slot: " + slot);
    }

    private static void checkMultiplier(int multiplier) {
        if (multiplier <= 0 || multiplier > StorageContents.MAX_COUNT)
            throw new IllegalArgumentException("Invalid actual backpack multiplier: " + multiplier);
    }

    private static boolean ghost(int slot) {
        return LAYOUT.magnetFilter().contains(slot) || LAYOUT.feederFilter().contains(slot)
                || LAYOUT.thirstFilter().contains(slot) || LAYOUT.voidFilter().contains(slot);
    }

    /** A generic empty-slot hint uses 64; non-empty resources retain component-defined stack limits. */
    public static int slotCapacity(int slot, ItemStack stack, int multiplier) {
        checkSlot(slot);
        checkMultiplier(multiplier);
        int nativeLimit = stack.isEmpty() ? 64 : stack.getMaxStackSize();
        if (nativeLimit <= 0) throw new IllegalArgumentException("Invalid item stack limit");
        long capacity = nativeLimit;
        if (LAYOUT.items().contains(slot)) capacity *= multiplier;
        else if (LAYOUT.upgrades().contains(slot) || LAYOUT.jukeboxDiscs().contains(slot)
                || LAYOUT.jetpackModifier().contains(slot) || ghost(slot)
                || slot == LAYOUT.workshop().getStartIndex() + WorkshopUpgrade.MACHINE_SLOT) capacity = 1;
        if (capacity > StorageContents.MAX_COUNT) throw new IllegalArgumentException("Backpack slot capacity exceeds private quantity bound");
        return (int) capacity;
    }

    /** Validates a stored quantity, not an insertion request which may be split across slots. */
    public static void validateStack(int slot, ItemStack stack, int multiplier) {
        Objects.requireNonNull(stack, "stack");
        int capacity = slotCapacity(slot, stack, multiplier);
        if (stack.isEmpty()) return;
        if (!ghost(slot) && stack.getItem() instanceof BackpackItem)
            throw new IllegalArgumentException("A backpack cannot contain another backpack in physical slot " + slot);
        if (stack.getCount() <= 0 || stack.getCount() > capacity)
            throw new IllegalArgumentException("Backpack slot " + slot + " quantity " + stack.getCount() + " exceeds " + capacity);
        var validation = ItemStack.validateStrict(stack.copyWithCount(1));
        if (validation.isError())
            throw new IllegalArgumentException("Invalid stored item components: " + validation.error().orElseThrow().message());
        if (LAYOUT.upgrades().contains(slot) && UpgradeType.fromItem(stack.getItem()) == null)
            throw new IllegalArgumentException("Non-upgrade item in backpack upgrade slot " + slot);
        if (LAYOUT.jukeboxDiscs().contains(slot) && !stack.has(DataComponents.JUKEBOX_PLAYABLE))
            throw new IllegalArgumentException("Non-playable item in backpack disc slot");
        if (LAYOUT.jetpackModifier().contains(slot) && !knownJetpackModifier(stack))
            throw new IllegalArgumentException("Unknown backpack jetpack modifier");
        int workshop = LAYOUT.workshop().getStartIndex();
        if (slot == workshop + WorkshopUpgrade.MACHINE_SLOT && !WorkshopRecipeHelper.isMachine(stack))
            throw new IllegalArgumentException("Non-machine item in backpack workshop machine slot");
        if (slot == workshop + WorkshopUpgrade.BACKTANK_SLOT && !WorkshopRecipeHelper.isBacktank(stack))
            throw new IllegalArgumentException("Non-backtank item in backpack workshop air slot");
    }

    // Loading an installed modifier remains valid when its gameplay feature is temporarily disabled.
    private static boolean knownJetpackModifier(ItemStack stack) {
        for (JetpackModifier modifier : JetpackModifier.values())
            if (modifier != JetpackModifier.NONE && ItemStack.isSameItem(modifier.getDisplayStack(), stack)) return true;
        return false;
    }

    /** Validate the whole document before a fixed-size live inventory is changed. */
    public static void validateContents(StorageContents contents, int multiplier) {
        Objects.requireNonNull(contents, "contents");
        checkMultiplier(multiplier);
        if (contents.getSlots() < 0 || contents.getSlots() > SLOT_COUNT)
            throw new IllegalArgumentException("Private inventory exceeds fixed backpack layout");
        for (int slot = 0; slot < contents.getSlots(); slot++)
            validateStack(slot, contents.getStackInSlot(slot), multiplier);
    }
}
