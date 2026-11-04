package com.george_vi.electroenergetics.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public final class EntityEntry<T extends Entity>
        extends RegistryEntry<EntityType<?>, EntityType<T>> {
    public EntityEntry(Identifier id, Supplier<? extends EntityType<T>> factory) {
        super(id, factory);
    }
}
