package com.railwayteam.railways.content.conductor.toolbox.fabric;

import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class MountedToolboxEquipPacketImpl {
    public static void doEquip(ServerPlayer player, int hotbarSlot, ItemStack held, ToolboxInventory inv) {
        ItemStack remaining = held.copy();
        for (int compartment = 0; compartment < 8 && !remaining.isEmpty(); compartment++) {
            ItemStack remainder = inv.distributeToCompartment(remaining, compartment, false);
            int inserted = remaining.getCount() - remainder.getCount();
            if (inserted > 0)
                remaining.shrink(inserted);
        }
        if (remaining.getCount() != held.getCount())
            player.getInventory().setItem(hotbarSlot, remaining);
    }
}
