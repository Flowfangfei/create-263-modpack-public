package com.george_vi.electroenergetics.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public final class BlockEntry<T extends Block> extends RegistryEntry<Block, T> implements ItemLike {
    public BlockEntry(Identifier id, Supplier<? extends T> factory) {
        super(id, factory);
    }

    public boolean has(BlockState state) {
        return state.is(get());
    }

    public boolean isIn(ItemStack stack) {
        return stack.is(asItem());
    }

    public ItemStack asStack() {
        return new ItemStack(asItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(asItem(), count);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }

    public BlockState getDefaultState() {
        return get().defaultBlockState();
    }
}
