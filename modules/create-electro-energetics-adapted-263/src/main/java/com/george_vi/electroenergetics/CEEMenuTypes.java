package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.electrical_panel.link.*;
import com.george_vi.electroenergetics.registry.RegistryEntry;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.foundation.gui.menu.MenuType;

import net.minecraft.core.Registry;

public final class CEEMenuTypes {
    public static final RegistryEntry<MenuType<?>, MenuType<ElectricalPanelLink>>
            ELECTRICAL_PANEL_LINK_MENU =
                    register("electrical_panel_link", ElectricalPanelLinkMenu::create);
    public static final RegistryEntry<MenuType<?>, MenuType<ElectricalPanelLink>>
            BIDIRECTIONAL_LINK_MENU = register("bidirectional_link", BiDirectionalLinkMenu::create);

    private static RegistryEntry<MenuType<?>, MenuType<ElectricalPanelLink>> register(
            String name, MenuType<ElectricalPanelLink> type) {
        Registry.register(CreateRegistries.MENU_TYPE, CreateElectroEnergetics.rl(name), type);
        return new RegistryEntry<>(CreateElectroEnergetics.rl(name), () -> type);
    }

    public static void register() {}
}
