package com.simibubi.create.foundation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface CustomBlockEntityTagItem {
   boolean updateCustomBlockEntityTag(BlockPos var1, Level var2, Player var3, ItemStack var4, BlockState var5);
}
