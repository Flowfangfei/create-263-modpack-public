package com.simibubi.create.foundation.item;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant;

import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;

public final class CookingFuelHelper {
	private static final ContextKeySet CONTEXT = new ContextKeySet.Builder()
		.required(LootContextParams.ORIGIN)
		.required(LootContextParams.BLOCK_STATE)
		.optional(LootContextParams.BLOCK_ENTITY)
		.required(NeoForgeLootContextParams.QUERIED_STACK)
		.build();

	public static int burnTime(Level level, BlockPos pos, ItemStack stack) {
		CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
		if (fuel == null || level == null)
			return 0;
		if (level instanceof ServerLevel server) {
			LootParams params = new LootParams.Builder(server)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
				.withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(pos))
				.withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos))
				.withParameter(NeoForgeLootContextParams.QUERIED_STACK, stack)
				.create(CONTEXT);
			return ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime,
				new LootContext.Builder(params).create(Optional.empty()), 0);
		}
		return fuel.burnTime() instanceof Constant constant ? constant.value() : 1;
	}
}
