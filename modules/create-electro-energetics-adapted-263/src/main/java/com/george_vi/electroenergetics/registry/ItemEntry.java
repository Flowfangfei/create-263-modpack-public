package com.george_vi.electroenergetics.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public final class ItemEntry<T extends Item> extends RegistryEntry<Item, T> implements ItemLike {
    public ItemEntry(Identifier id, Supplier<? extends T> factory) {
        super(id, factory);
    }

    public boolean isIn(ItemStack stack) {
        return stack.is(get());
    }

    public ItemStack asStack() {
        return new ItemStack(get());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(get(), count);
    }

    @Override
    public Item asItem() {
        return get();
    }
}
