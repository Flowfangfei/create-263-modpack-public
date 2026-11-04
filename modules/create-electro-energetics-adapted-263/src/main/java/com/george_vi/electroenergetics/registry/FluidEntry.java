package com.george_vi.electroenergetics.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;
import java.util.function.Supplier;

public final class FluidEntry<T extends Fluid> extends RegistryEntry<Fluid, T> {
    private final Supplier<? extends Fluid> source;

    public FluidEntry(
            Identifier id, Supplier<? extends T> factory, Supplier<? extends Fluid> source) {
        super(id, factory);
        this.source = source;
    }

    public Fluid getSource() {
        return source.get();
    }

    public Optional<Item> getBucket() {
        return Optional.ofNullable(getSource().getBucket());
    }

    public boolean is(Fluid fluid) {
        return get().isSame(fluid);
    }
}
