package com.george_vi.electroenergetics.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class BlockEntityEntry<T extends BlockEntity>
        extends RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> {
    public BlockEntityEntry(Identifier id, Supplier<? extends BlockEntityType<T>> factory) {
        super(id, factory);
    }
}
