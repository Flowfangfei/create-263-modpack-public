package net.fxnt.fxntstorage.backpack.inventory;

import net.fxnt.fxntstorage.backpack.BackpackBlock;
import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.client.menu.BackpackMenu;
import net.fxnt.fxntstorage.backpack.upgrade.*;
import net.fxnt.fxntstorage.backpack.upgrade.voiding.VoidUpgrade;
import net.fxnt.fxntstorage.backpack.util.BackpackHelper;
import net.fxnt.fxntstorage.init.ModAttachmentTypes;
import net.fxnt.fxntstorage.init.ModDataComponents;
import net.fxnt.fxntstorage.network.packet.SetActivePanelPacket;
import net.fxnt.fxntstorage.network.packet.SetSortOrderPacket;
import net.fxnt.fxntstorage.network.packet.UpgradeDataPacket;
import net.fxnt.fxntstorage.network.packet.UpgradeIntDataPacket;
import net.fxnt.fxntstorage.util.SortOrder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.fxnt.fxntstorage.inventory.StorageContents;
import net.fxnt.fxntstorage.inventory.BackpackInventoryRules;
import com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable;
import com.simibubi.create.compat.neoforge263.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BackpackContainer implements IBackpackContainer, IItemHandlerModifiable {
    private final BackpackSlotLayout layout = BackpackSlotLayout.createLayout();
    private final int CONTAINER_SIZE = layout.getTotalSlots();

    private Player player;
    private ItemStack stack;
    private boolean synchronizingContents;
    private StorageContents lastReadContents;
    private final ItemStackHandler itemHandler = new net.fxnt.fxntstorage.inventory.StorageInventoryHandler(CONTAINER_SIZE) {
        @Override
        public ItemStack getStackInSlot(int slot) {
            refreshContents();
            return super.getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack value) {
            refreshContents();
            super.setStackInSlot(slot, value);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack value, boolean simulate) {
            refreshContents();
            return super.insertItem(slot, value, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            refreshContents();
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            publishContents();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack value) {
            return BackpackContainer.this.isItemValid(slot, value);
        }

        @Override
        public void validateContents(StorageContents contents) {
            BackpackInventoryRules.validateContents(contents, stackMultiplier);
        }

        @Override
        protected void validateStackInSlot(int slot, ItemStack value) {
            BackpackInventoryRules.validateStack(slot, value, stackMultiplier);
        }

        @Override
        protected int restoredSlotCount(StorageContents contents) {
            return CONTAINER_SIZE;
        }

        @Override
        public int getSlotLimit(int slot) {
            return BackpackInventoryRules.slotCapacity(slot, getStackInSlot(slot), stackMultiplier);
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return BackpackInventoryRules.slotCapacity(slot, stack, stackMultiplier);
        }
    };
    private final NonNullList<String> upgrades = NonNullList.create();

    private int stackMultiplier;
    private SortOrder sortOrder = SortOrder.COUNT;

    // Unified upgrade data management
    private final UpgradeDataManager upgradeData = new UpgradeDataManager();

    public BackpackContainer(@Nullable Player player, ItemStack itemStack) {
        this.player = player;
        this.stack = itemStack;
        loadItemsFromStack(itemStack);
    }

    public void setContext(ItemStack itemStack, @Nullable Player player) {
        this.player = player;
        if (this.stack != itemStack) {
            this.stack = itemStack;
            loadItemsFromStack(itemStack);
        } else {
            stackMultiplier = ((BackpackBlock)((BackpackItem)itemStack.getItem()).getBlock()).getStackMultiplier();
            refreshContents();
        }
    }

    public ItemStackHandler getItemHandler() {
        refreshContents();
        return itemHandler;
    }

    // Native transfer transactions replace the physical stack's immutable component. A cached
    // menu must observe that replacement before it reads or changes its mutable inventory view.
    private void refreshContents() {
        if (synchronizingContents || stack == null) return;
        StorageContents current = stack.getOrDefault(ModDataComponents.STORAGE_CONTENTS, StorageContents.EMPTY);
        if (!Objects.equals(current, lastReadContents)) readInventory(current);
    }

    private void publishContents() {
        if (synchronizingContents || stack == null) return;
        synchronizingContents = true;
        try {
            StorageContents current = StorageContents.fromHandler(itemHandler);
            stack.set(ModDataComponents.STORAGE_CONTENTS, current);
            lastReadContents = current;
        } finally {
            synchronizingContents = false;
        }
    }

    public void readInventory(StorageContents contents) {
        BackpackInventoryRules.validateContents(contents, stackMultiplier);
        int slots = itemHandler.getSlots();
        int contentSlots = contents.getSlots();
        if(contentSlots>slots)throw new IllegalArgumentException("Inventory component exceeds backpack slots");
        boolean previous = synchronizingContents;
        synchronizingContents = true;
        try {
            for (int i = 0; i < slots; i++) {
                itemHandler.setStackInSlot(i, contents.getStackInSlot(i));
            }
            lastReadContents = contents;
        } finally {
            synchronizingContents = previous;
        }
    }

    public void loadItemsFromStack(ItemStack itemStack) {
        stackMultiplier = ((BackpackBlock)((BackpackItem)itemStack.getItem()).getBlock()).getStackMultiplier();
        readInventory(itemStack.getOrDefault(net.fxnt.fxntstorage.init.ModDataComponents.STORAGE_CONTENTS, StorageContents.EMPTY));

        // Load upgrades list
        upgrades.clear();
        if (itemStack.getComponents().has(ModDataComponents.BACKPACK_UPGRADES)) {
            List<String> upgradeList = itemStack.getComponents().get(ModDataComponents.BACKPACK_UPGRADES);
            if (upgradeList != null) {
                upgrades.addAll(upgradeList);
            }
        }

        // Load sort order
        sortOrder = Optional.ofNullable(
                itemStack.getComponents().get(ModDataComponents.INVENTORY_SORT_ORDER)
        ).orElse(SortOrder.COUNT);

        // Load upgrade data (panels and settings) and copy into our manager
        UpgradeDataManager loadedData = UpgradeDataManager.loadFromItem(itemStack);
        upgradeData.copyFrom(loadedData);
    }

    // Detect a stale client-side worn container after the equipped stack is re-synced
    public boolean matchesUpgrades(ItemStack itemStack) {
        List<String> other = itemStack.get(ModDataComponents.BACKPACK_UPGRADES);
        return Objects.equals(upgrades, other == null ? List.of() : other);
    }

    public void saveItemsToStack() {
        refreshContents();
        publishContents();
        this.stack.set(ModDataComponents.BACKPACK_UPGRADES, upgrades);
        this.stack.set(ModDataComponents.BACKPACK_STACK_MULTIPLIER, stackMultiplier);
        this.stack.set(ModDataComponents.INVENTORY_SORT_ORDER, sortOrder);

        // Save upgrade data
        upgradeData.saveToItem(stack);
    }

    public void saveSettings() {
        upgradeData.saveToItem(stack);
        setChanged();
    }

    public List<ItemStack> getItems() {
        List<ItemStack> itemList = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); ++i) {
            itemList.add(itemHandler.getStackInSlot(i));
        }
        return itemList;
    }

    @Override
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        if (player != null && player.level().isClientSide())
            net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new SetSortOrderPacket(sortOrder));
        setDataChanged();
    }

    @Override
    public boolean isPanelExpanded(UpgradeType type) {
        return upgradeData.isPanelExpanded(type);
    }

    @Override
    public void togglePanelExpanded(UpgradeType type) {
        upgradeData.togglePanel(type);
        if (player != null && player.level().isClientSide()) {
            net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new SetActivePanelPacket(type));
        } else if (player != null) {
            setDataChanged();
        }
    }

    @Override
    public void clearPanelExpanded(UpgradeType type) {
        upgradeData.clearPanel(type);
        if (player != null && !player.level().isClientSide()) {
            setDataChanged();
        }
    }

    @Override
    public UpgradeType getExpandedPanel() {
        return upgradeData.getExpandedPanel();
    }

    @Override
    public void setExpandedPanel(UpgradeType type) {
        upgradeData.setExpandedPanel(type);
        setDataChanged();
    }

    @Override
    public boolean getUpgradeSetting(UpgradeDataSync.Field setting) {
        return upgradeData.getSetting(setting);
    }

    @Override
    public void setUpgradeSetting(UpgradeDataSync.Field setting, boolean value) {
        upgradeData.setSetting(setting, value);
        if (player != null && player.level().isClientSide())
            net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new UpgradeDataPacket(setting.getIndex(), value));
        setDataChanged();
    }

    @Override
    public int getUpgradeIntSetting(UpgradeDataSync.Field setting) {
        return upgradeData.getIntSetting(setting);
    }

    @Override
    public void setUpgradeIntSetting(UpgradeDataSync.Field setting, int value) {
        upgradeData.setIntSetting(setting, value);
        if (player != null && player.level().isClientSide())
            net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new UpgradeIntDataPacket(setting.getIndex(), value));
        setDataChanged();
    }

    @Override
    public int getSlots() {
        return itemHandler.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        return itemHandler.getStackInSlot(i);
    }

    @Override
    public void setStackInSlot(int i, @NotNull ItemStack itemStack) {
        itemHandler.setStackInSlot(i, itemStack);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack itemStack, boolean simulate) {
        if(!isItemValid(slot,itemStack))return itemStack;
        ItemStack toInsert = player != null && layout.items().contains(slot)
                ? VoidUpgrade.acceptableInsert(this, player.level(), itemStack)
                : itemStack;
        if (toInsert.isEmpty()) return ItemStack.EMPTY;

        ItemStack remainder = itemHandler.insertItem(slot, toInsert, simulate);
        if (!simulate && remainder.getCount() != toInsert.getCount())
            saveItemsToStack();
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack result = itemHandler.extractItem(slot, amount, simulate);
        if (!simulate && !result.isEmpty())
            saveItemsToStack();
        return result;
    }

    @Override
    public int getSlotLimit(int i) {
        return itemHandler.getSlotLimit(i);
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
        return !(itemStack.getItem() instanceof BackpackItem);
    }

    @Override
    public int getStackMultiplier() {
        return stackMultiplier;
    }

    @Override
    public void setPlayerInteraction(boolean isPlayer) {
        // noop
    }

    @Override
    public void setDataChanged() {
        if (player == null || player.level().isClientSide()) return;
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack selectedStack = player.getInventory().getSelectedItem();
        if (selectedStack.getItem() instanceof BackpackItem) {
            return true;
        }
        return BackpackHelper.isWearingBackpack(player);
    }

    public void refreshUpgrades() {
        upgrades.clear();
        upgrades.addAll(UpgradeHelper.refreshUpgradeList(itemHandler));
    }

    public void setChanged() {
        if (player == null || player.level().isClientSide()) return;

        // External automation only exposes the 108 item slots. Upgrade state belongs to
        // this private menu inventory and must never be indexed through that capability.
        refreshContents();
        refreshUpgrades();
        Set<UpgradeType> installedTypes = new HashSet<>(UpgradeHelper.getInstalledUpgrades(itemHandler));
        for (UpgradeType type : UpgradeType.values()) {
            if (installedTypes.contains(type)) continue;
            IUpgrade upgrade = UpgradeRegistry.get(type);
            if (upgrade != null) {
                for (UpgradeDataSync.Field field : upgrade.getSettings()) upgradeData.clearSetting(field);
            }
            upgradeData.clearPanel(type);
        }

        saveItemsToStack();

        boolean openMenuShowsThis = player.containerMenu instanceof BackpackMenu menu && menu.container == this;
        boolean isCachedWornContainer =
                player.getData(ModAttachmentTypes.WORN_BACKPACK_CONTAINER) == this;
        if (!openMenuShowsThis && !isCachedWornContainer) {
            BackpackContainer.Cache.invalidateWornBackpack(player);
        }
    }

    public static class Cache {
        public static BackpackContainer getOrCreateWornBackpack(Player player, ItemStack backpack) {
            BackpackContainer cached = player.getData(ModAttachmentTypes.WORN_BACKPACK_CONTAINER);

            if (cached != null) {
                if (player.level().isClientSide() && !cached.matchesUpgrades(backpack)) {
                    BackpackContainer rebuilt = new BackpackContainer(player, backpack);
                    player.setData(ModAttachmentTypes.WORN_BACKPACK_CONTAINER, rebuilt);
                    return rebuilt;
                }
                cached.setContext(backpack, player);
                return cached;
            }

            BackpackContainer newContainer = new BackpackContainer(player, backpack);
            player.setData(ModAttachmentTypes.WORN_BACKPACK_CONTAINER, newContainer);
            return newContainer;
        }

        public static void invalidateWornBackpack(Player player) {
            if (player.hasData(ModAttachmentTypes.WORN_BACKPACK_CONTAINER))
                player.removeData(ModAttachmentTypes.WORN_BACKPACK_CONTAINER);
        }

        public static BackpackContainer getForCapability(ItemStack backpack) {
            return new BackpackContainer(null, backpack);
        }
    }
}
