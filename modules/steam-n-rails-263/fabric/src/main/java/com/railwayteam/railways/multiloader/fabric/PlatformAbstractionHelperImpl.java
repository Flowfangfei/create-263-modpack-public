package com.railwayteam.railways.multiloader.fabric;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.CookingFuelHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

public class PlatformAbstractionHelperImpl {
    public static int getBurnTime(Item item) {
        var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if(server!=null)return CookingFuelHelper.burnTime(server.overworld(),net.minecraft.core.BlockPos.ZERO,new ItemStack(item));
        // The remote client has no server-side FuelValues table. Preserve the
        // vanilla liquid-fuel case and let the authoritative server validate modded fuels.
        return item == Items.LAVA_BUCKET ? 20_000 : 0;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> ArgumentType<T> enumArgument(Class<T> enumClass) {
        return (ArgumentType) StringArgumentType.word();
    }
}
