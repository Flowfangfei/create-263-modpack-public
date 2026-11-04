package com.george_vi.electroenergetics.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Registration queue with explicit lifecycle; never subscribes to a loader event bus. */
public final class DeferredRegister<R> {
    private final Registry<R> registry;
    private final String namespace;
    private final List<RegistryEntry<R, ? extends R>> entries = new ArrayList<>();
    private boolean registered;

    private DeferredRegister(Registry<R> registry, String namespace) {
        this.registry = registry;
        this.namespace = namespace;
    }

    public static <R> DeferredRegister<R> create(Registry<R> registry, String namespace) {
        return new DeferredRegister<>(registry, namespace);
    }

    @SuppressWarnings("unchecked")
    public static <R> DeferredRegister<R> create(
            ResourceKey<? extends Registry<R>> key, String namespace) {
        return create(
                (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.identifier()), namespace);
    }

    public <T extends R> RegistryEntry<R, T> register(String name, Supplier<? extends T> factory) {
        if (registered)
            throw new IllegalStateException("Registry already initialized: " + namespace);
        RegistryEntry<R, T> entry =
                new RegistryEntry<>(Identifier.fromNamespaceAndPath(namespace, name), factory);
        entries.add(entry);
        return entry;
    }

    public void register() {
        if (registered) return;
        for (RegistryEntry<R, ? extends R> entry : entries) {
            Registry.register(registry, entry.getId(), entry.get());
        }
        registered = true;
    }
}
