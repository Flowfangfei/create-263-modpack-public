package net.fxnt.fxntstorage.util;

import com.simibubi.create.compat.neoforge263.items.IItemHandler;
import com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable;
import net.fxnt.fxntstorage.backpack.BackpackBlock;
import net.fxnt.fxntstorage.backpack.BackpackEntity;
import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.inventory.BackpackSlotLayout;
import net.fxnt.fxntstorage.container.StorageBoxEntity;
import net.fxnt.fxntstorage.controller.StorageControllerEntity;
import net.fxnt.fxntstorage.controller.StorageInterfaceEntity;
import net.fxnt.fxntstorage.init.ModDataComponents;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxEntity;
import net.fxnt.fxntstorage.simple_storage.CompactingChain;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/** Native transactional views; every view of a physical box shares that box's journal. */
public final class StorageItemTransfer {
    private static final int BACKPACK_ITEM_SLOTS=BackpackSlotLayout.createLayout().items().getCount();
    private static final Map<BlockEntity, WeakReference<BoxJournal>> JOURNALS = new WeakHashMap<>();
    private StorageItemTransfer() {}

    public static ResourceHandler<ItemResource> block(BlockEntity entity, Supplier<IItemHandler> view) {
        return new View(entity, view);
    }

    public static ResourceHandler<ItemResource> backpack(ItemAccess access) {
        return new net.fxnt.fxntstorage.inventory.StorageBackpackTransfer(access);
    }

    private static synchronized BoxJournal journal(BlockEntity entity) {
        WeakReference<BoxJournal> reference = JOURNALS.get(entity);
        BoxJournal journal = reference == null ? null : reference.get();
        if (journal == null) {
            journal = new BoxJournal(entity);
            JOURNALS.put(entity, new WeakReference<>(journal));
        }
        return journal;
    }

    private static IItemHandlerModifiable inventory(BlockEntity entity) {
        if (entity instanceof SimpleStorageBoxEntity box) return box.getItemHandler();
        if (entity instanceof ReserveStorageBoxEntity box) return box.getItemHandler();
        if (entity instanceof StorageBoxEntity box) return box.getItemHandler();
        if (entity instanceof BackpackEntity box) return box.getItemHandler();
        throw new IllegalArgumentException("No physical storage for " + entity.getClass());
    }

    private record Contents(ItemStack[] stacks, ItemStack filter, CompactingChain chain, int capacity,
                            int stored, int selectedTier) {}

    private static final class BoxJournal extends SnapshotJournal<Contents> {
        private final BlockEntity entity;
        BoxJournal(BlockEntity entity) { this.entity = entity; }
        @Override protected Contents createSnapshot() {
            IItemHandlerModifiable handler = inventory(entity);
            ItemStack[] stacks = new ItemStack[handler.getSlots()];
            for (int i = 0; i < stacks.length; i++) stacks[i] = handler.getStackInSlot(i).copy();
            if (entity instanceof SimpleStorageBoxEntity box)
                return new Contents(stacks, box.filterItem.copy(), box.compactingChain, box.maxItemCapacity,
                        box.storedAmount, box.compactingSelectedTier);
            return new Contents(stacks, ItemStack.EMPTY, null, 0, 0, 0);
        }
        @Override protected void revertToSnapshot(Contents snapshot) {
            IItemHandlerModifiable handler = inventory(entity);
            for (int i = 0; i < snapshot.stacks.length; i++)
                if (!ItemStack.matches(handler.getStackInSlot(i), snapshot.stacks[i]))
                    handler.setStackInSlot(i, snapshot.stacks[i].copy());
            if (entity instanceof SimpleStorageBoxEntity box) {
                // Restore physical T0 storage once, then its derived metadata; never write virtual T1/T2 slots.
                box.filterItem = snapshot.filter.copy();
                box.compactingChain = snapshot.chain;
                box.maxItemCapacity = snapshot.capacity;
                box.storedAmount = snapshot.stored;
                box.compactingSelectedTier = snapshot.selectedTier;
            }
            entity.setChanged();
        }
        @Override protected void onRootCommit(Contents snapshot) { entity.setChanged(); }
    }

    private static final class View implements ResourceHandler<ItemResource> {
        private final BlockEntity owner;
        private final Supplier<IItemHandler> view;
        View(BlockEntity owner, Supplier<IItemHandler> view) { this.owner = owner; this.view = view; }
        private IItemHandler handler() { return view.get(); }
        private void snapshot(TransactionContext transaction) {
            StorageControllerEntity controller = owner instanceof StorageControllerEntity c ? c
                    : owner instanceof StorageInterfaceEntity i ? i.controller : null;
            if (owner instanceof StorageControllerEntity || owner instanceof StorageInterfaceEntity) {
                if (controller == null || controller.getConnectedNetwork() == null) return;
                for (var box : controller.getConnectedNetwork().getBoxes())
                    journal(box.simpleStorageBoxEntity).updateSnapshots(transaction);
            } else journal(owner).updateSnapshots(transaction);
        }
        @Override public int size() {
            return owner instanceof BackpackEntity ? BACKPACK_ITEM_SLOTS : handler().getSlots();
        }
        private IItemHandler checkedHandler(int index) {
            java.util.Objects.checkIndex(index, size());
            return handler();
        }
        @Override public ItemResource getResource(int index) { return ItemResource.of(checkedHandler(index).getStackInSlot(index)); }
        @Override public long getAmountAsLong(int index) { return checkedHandler(index).getStackInSlot(index).getCount(); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) {
            IItemHandler current=checkedHandler(index);
            if(!resource.isEmpty()&&!isValid(index,resource))return 0;
            if(owner instanceof BackpackEntity backpack){
                int multiplier=((BackpackBlock)backpack.getBlockState().getBlock()).getStackMultiplier();
                return net.fxnt.fxntstorage.inventory.BackpackInventoryRules.slotCapacity(index,resource.isEmpty()?ItemStack.EMPTY:resource.toStack(1),multiplier);
            }
            return current.getSlotLimit(index);
        }
        @Override public boolean isValid(int index, ItemResource resource) {
            IItemHandler current=checkedHandler(index);
            return resource.isEmpty() || current.isItemValid(index,resource.toStack(1));
        }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (amount <= 0 || resource.isEmpty() || !isValid(index, resource)) return 0;
            IItemHandler handler = checkedHandler(index);
            int accepted = amount - handler.insertItem(index, resource.toStack(amount), true).getCount();
            if (accepted <= 0) return 0;
            snapshot(transaction);
            return accepted - handler.insertItem(index, resource.toStack(accepted), false).getCount();
        }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (amount <= 0 || resource.isEmpty()) return 0;
            IItemHandler handler = checkedHandler(index);
            if (!resource.matches(handler.getStackInSlot(index))) return 0;
            ItemStack available = handler.extractItem(index, amount, true);
            if (available.isEmpty()) return 0;
            snapshot(transaction);
            return handler.extractItem(index, available.getCount(), false).getCount();
        }
    }
}
