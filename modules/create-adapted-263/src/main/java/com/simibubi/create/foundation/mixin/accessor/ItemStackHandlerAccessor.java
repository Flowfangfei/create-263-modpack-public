package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import com.simibubi.create.compat.neoforge263.items.ItemStackHandler;

@Mixin(ItemStackHandler.class)
public interface ItemStackHandlerAccessor {
	@Accessor("stacks")
	NonNullList<ItemStack> create$getStacks();
}
