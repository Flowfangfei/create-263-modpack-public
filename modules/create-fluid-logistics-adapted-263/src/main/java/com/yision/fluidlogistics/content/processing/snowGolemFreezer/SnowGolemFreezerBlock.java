package com.yision.fluidlogistics.content.processing.snowGolemFreezer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.yision.fluidlogistics.content.fluids.itemTransfer.FluidContainerInteraction;
import com.yision.fluidlogistics.registry.AllBlockEntities;
import com.yision.fluidlogistics.registry.AllBlocks;
import com.yision.fluidlogistics.registry.AllItems;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SnowGolemFreezerBlock extends BlazeBurnerBlock {


	public SnowGolemFreezerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntityType<? extends BlazeBurnerBlockEntity> getBlockEntityType() {
		return AllBlockEntities.SNOW_GOLEM_FREEZER.get();
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
		return getFreezerStack(state);
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
		return new ItemRequirement(ItemUseType.CONSUME, getFreezerStack(state));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean filled = context.getItemInHand().getItem() instanceof SnowGolemFreezerBlockItem item
			&& item.hasCapturedSnowGolem();
		return defaultBlockState().setValue(HEAT_LEVEL, filled ? HeatLevel.SMOULDERING : HeatLevel.NONE)
			.setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	private static ItemStack getFreezerStack(BlockState state) {
		return state.getValue(HEAT_LEVEL) == HeatLevel.NONE
			? AllItems.EMPTY_SNOW_GOLEM_FREEZER.asStack() : AllBlocks.SNOW_GOLEM_FREEZER.asStack();
	}
    // The pinned generated loot table is bundled in resources.


	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return level.getBlockEntity(pos) instanceof SnowGolemFreezerBlockEntity cooler
			? Math.max(0, cooler.getHeatLevelFromBlock().ordinal() - 1) : 0;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (state.getValue(HEAT_LEVEL) == HeatLevel.NONE)
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (stack.is(Items.CARVED_PUMPKIN)
			&& level.getBlockEntity(pos) instanceof SnowGolemFreezerBlockEntity cooler) {
			if (!level.isClientSide())
				cooler.applyPumpkinHead();
			return InteractionResult.SUCCESS;
		}
		if (level.getBlockEntity(pos) instanceof SnowGolemFreezerBlockEntity cooler
			&& !cooler.stockKeeper
			&& !FluidContainerInteraction.tryEmptyItem(level, player, hand, stack,
				cooler.getFuelInput(), fluid -> SnowGolemFreezerFuelManager.find(fluid) != null,
				false, cooler::setChanged).isEmpty()) {
			if (level.isClientSide())
				cooler.spawnParticleBurst(false);
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}
}
