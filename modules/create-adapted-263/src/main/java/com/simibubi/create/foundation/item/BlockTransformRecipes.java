package com.simibubi.create.foundation.item;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.createmod.catnip.api.level.wrapper.WrappedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.BlockTransformer.BlockTransformData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTransformRecipes {
	@Nullable
	public static BlockState stripped(@Nullable Level level, BlockState state) {
		return transform(level, state, SoundEvents.AXE_STRIP.value());
	}

	@Nullable
	public static BlockState transform(@Nullable Level level, BlockState state, SoundEvent sound) {
		if (level == null)
			return null;
		Holder<BlockTransformer> transformer = new ItemStack(Items.IRON_AXE).get(DataComponents.BLOCK_TRANSFORMER);
		if (transformer == null)
			return null;
		WrappedLevel wrapped = new WrappedLevel(level) {
			@Override
			public BlockState getBlockState(BlockPos pos) {
				return pos.equals(BlockPos.ZERO) ? state : super.getBlockState(pos);
			}

			@Override
			public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
				return predicate.test(getBlockState(pos));
			}
		};
		for (BlockTransformData transform : transformer.value().transforms()) {
			if (transform.sound().value() != sound)
				continue;
			BlockState result = transform.blockStateProvider().value().getState(wrapped, level.getRandom(), BlockPos.ZERO);
			if (result != null && result != state)
				return result;
		}
		return null;
	}
}
