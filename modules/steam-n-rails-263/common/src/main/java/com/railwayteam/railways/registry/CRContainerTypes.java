package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolboxContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class CRContainerTypes {
    public static final MenuType<MountedToolboxContainer> MOUNTED_TOOLBOX=Registry.register(
        BuiltInRegistries.MENU,Railways.asResource("mounted_toolbox"),IMenuTypeExtension.create(MountedToolboxContainer::new));
    private CRContainerTypes() { }
    public static void register() { }
    @SuppressWarnings({"unchecked","rawtypes"})
    public static void registerScreens(RegisterMenuScreensEvent event) {
        MenuType<com.simibubi.create.content.equipment.toolbox.ToolboxMenu> type=(MenuType)(Object)MOUNTED_TOOLBOX;
        event.register(type,com.simibubi.create.content.equipment.toolbox.ToolboxScreen::new);
    }
}
