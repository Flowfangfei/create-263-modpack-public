/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class CEICodecs {
    public static final Codec<EnchantmentInstance> ENCHANTMENT_INSTANCE = RecordCodecBuilder
            .create(instance -> instance.group(
                    Enchantment.CODEC.fieldOf("id").forGetter(it -> it.enchantment()),
                    Codec.intRange(0, 255).fieldOf("level").forGetter(it -> it.level())).apply(instance, EnchantmentInstance::new));

    public static net.minecraft.nbt.Tag saveStack(net.minecraft.world.item.ItemStack stack, net.minecraft.core.HolderLookup.Provider registries) {
        return net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).getOrThrow();
    }
    public static net.minecraft.world.item.ItemStack loadStack(net.minecraft.core.HolderLookup.Provider registries, net.minecraft.nbt.CompoundTag tag) {
        return net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag).result().orElse(net.minecraft.world.item.ItemStack.EMPTY);
    }

    /** Transactional view over the real inventory, including rollback of every slot. */
    public static final class ItemHandlerResourceHandler extends net.neoforged.neoforge.transfer.transaction.SnapshotJournal<java.util.List<net.minecraft.world.item.ItemStack>> implements net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> {
        private final com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable inventory;
        public ItemHandlerResourceHandler(com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable inventory) { this.inventory = inventory; }
        public int size() { return inventory.getSlots(); }
        public net.neoforged.neoforge.transfer.item.ItemResource getResource(int slot) { return net.neoforged.neoforge.transfer.item.ItemResource.of(inventory.getStackInSlot(slot)); }
        public long getAmountAsLong(int slot) { return inventory.getStackInSlot(slot).getCount(); }
        public long getCapacityAsLong(int slot, net.neoforged.neoforge.transfer.item.ItemResource resource) { return Math.min(inventory.getSlotLimit(slot), resource.getMaxStackSize()); }
        public boolean isValid(int slot, net.neoforged.neoforge.transfer.item.ItemResource resource) { return !resource.isEmpty() && inventory.isItemValid(slot, resource.toStack()); }
        public int insert(int slot, net.neoforged.neoforge.transfer.item.ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.TransactionContext transaction) {
            if (amount <= 0 || resource.isEmpty()) return 0;
            int inserted = amount - inventory.insertItem(slot, resource.toStack(amount), true).getCount();
            if (inserted <= 0) return 0;
            updateSnapshots(transaction);
            return amount - inventory.insertItem(slot, resource.toStack(amount), false).getCount();
        }
        public int extract(int slot, net.neoforged.neoforge.transfer.item.ItemResource resource, int amount, net.neoforged.neoforge.transfer.transaction.TransactionContext transaction) {
            if (amount <= 0 || !resource.matches(inventory.getStackInSlot(slot))) return 0;
            int extracted = inventory.extractItem(slot, amount, true).getCount();
            if (extracted <= 0) return 0;
            updateSnapshots(transaction);
            return inventory.extractItem(slot, amount, false).getCount();
        }
        protected java.util.List<net.minecraft.world.item.ItemStack> createSnapshot() { return java.util.stream.IntStream.range(0, size()).mapToObj(slot -> inventory.getStackInSlot(slot).copy()).toList(); }
        protected void revertToSnapshot(java.util.List<net.minecraft.world.item.ItemStack> snapshot) { for (int slot = 0; slot < snapshot.size(); slot++) inventory.setStackInSlot(slot, snapshot.get(slot).copy()); }
    }
}
