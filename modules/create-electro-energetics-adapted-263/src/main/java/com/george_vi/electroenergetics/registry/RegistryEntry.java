package com.george_vi.electroenergetics.registry;

import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

/** Stable, lazily initialized handle used to break cycles between CEE registries. */
public class RegistryEntry<R, T extends R> implements Supplier<T> {
    private final Identifier id;
    private Supplier<? extends T> factory;
    private T value;

    public RegistryEntry(Identifier id, Supplier<? extends T> factory) {
        this.id = id;
        this.factory = factory;
    }

    @Override
    public synchronized T get() {
        if (value == null) {
            value = Objects.requireNonNull(factory.get(), "Registry entry " + id);
            factory = null;
        }
        return value;
    }

    public Identifier getId() {
        return id;
    }

    public T getEntry() {
        return get();
    }

    public boolean is(R other) {
        return get() == other;
    }
}
