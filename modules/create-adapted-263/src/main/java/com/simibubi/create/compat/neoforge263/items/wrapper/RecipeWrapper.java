/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.simibubi.create.compat.neoforge263.items.wrapper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import com.simibubi.create.compat.neoforge263.items.IItemHandler;

/**
 * @deprecated Implement {@link RecipeInput} directly instead.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public class RecipeWrapper implements RecipeInput {
    protected final IItemHandler inv;

    public RecipeWrapper(IItemHandler inv) {
        this.inv = inv;
    }

    /**
     * Returns the size of this inventory.
     */
    @Override
    public int size() {
        return inv.getSlots();
    }

    /**
     * Returns the stack in this slot. This stack should be a modifiable reference, not a copy of a stack in your inventory.
     */
    @Override
    public ItemStack getItem(int slot) {
        return inv.getStackInSlot(slot);
    }
}
