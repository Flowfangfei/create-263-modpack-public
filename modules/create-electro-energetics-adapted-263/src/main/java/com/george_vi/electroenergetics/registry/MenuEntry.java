package com.george_vi.electroenergetics.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class MenuEntry<T extends AbstractContainerMenu>
        extends RegistryEntry<MenuType<?>, MenuType<T>> {
    public MenuEntry(Identifier id, Supplier<? extends MenuType<T>> factory) {
        super(id, factory);
    }
}
