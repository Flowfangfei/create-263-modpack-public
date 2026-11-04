package com.yision.fluidlogistics.content.logistics.fluidPackager;

import static com.yision.fluidlogistics.registry.AllBlocks.COPPER_FROGPORT;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import com.yision.fluidlogistics.content.fluids.fluidPort.FluidPort;
import com.yision.fluidlogistics.registry.AllBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;

public class FluidPackagerBlock extends PackagerBlock {

	public FluidPackagerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferredFacing = null;
		Level level = context.getLevel();

		for (Direction face : context.getNearestLookingDirections()) {
			BlockPos adjacentPos = context.getClickedPos().relative(face);
			BlockEntity be = level.getBlockEntity(adjacentPos);
			if (be instanceof FluidPackagerBlockEntity)
				continue;

			var sidedHandler = com.yision.fluidlogistics.foundation.capability.LegacyCapabilityAccess.fluid(level, adjacentPos, face.getOpposite());
			var unsidedHandler = sidedHandler != null ? sidedHandler
				: com.yision.fluidlogistics.foundation.capability.LegacyCapabilityAccess.fluid(level, adjacentPos, null);
			if (unsidedHandler != null
				|| level.getBlockState(adjacentPos).hasProperty(BlockStateProperties.LEVEL_HONEY)) {
				preferredFacing = face.getOpposite();
				break;
			}
		}

		Player player = context.getPlayer();
		if (preferredFacing == null) {
			Direction facing = context.getNearestLookingDirection();
			preferredFacing = player != null && player.isShiftKeyDown() ? facing : facing.getOpposite();
		}

		if (player != null && !(player instanceof FakePlayer)) {
			BlockPos targetPos = context.getClickedPos().relative(preferredFacing.getOpposite());
			BlockState targetState = level.getBlockState(targetPos);
			if (AllBlocks.PORTABLE_FLUID_INTERFACE.has(targetState)) {
				player.sendOverlayMessage(CreateLang.translateDirect("fluid_packager.no_portable_fluid_interface"));
				return null;
			}
			if (level.getBlockEntity(targetPos) instanceof FluidPort port
				&& port.blocksFluidPackagerPlacement(preferredFacing)) {
				player.sendOverlayMessage(CreateLang.translateDirect("fluid_packager.no_portable_fluid_interface"));
				return null;
			}
		}

		return defaultBlockState()
			.setValue(POWERED, level.hasNeighborSignal(context.getClickedPos()))
			.setValue(FACING, preferredFacing);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (AllItems.WRENCH.isIn(stack))
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (com.yision.fluidlogistics.registry.AllItems.FLUID_FACTORY_GAUGE.isIn(stack))
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (AllBlocks.STOCK_LINK.isIn(stack) && !(state.hasProperty(LINKED) && state.getValue(LINKED)))
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (AllBlocks.PACKAGE_FROGPORT.isIn(stack) || COPPER_FROGPORT.isIn(stack))
			return InteractionResult.TRY_WITH_EMPTY_HAND;

		if (onBlockEntityUse(level, pos, be -> {
			if (be.heldBox.isEmpty()) {
				if (be.animationTicks > 0)
					return InteractionResult.SUCCESS;
				if (level.isClientSide())
					return InteractionResult.SUCCESS;
				if (!be.unwrapBox(stack.copy(), true))
					return InteractionResult.SUCCESS;
				be.unwrapBox(stack.copy(), false);
				be.triggerStockCheck();
				stack.shrink(1);
				AllSoundEvents.DEPOT_PLOP.playOnServer(level, pos);
				if (stack.isEmpty())
					player.setItemInHand(hand, ItemStack.EMPTY);
				return InteractionResult.SUCCESS;
			}
			if (be.animationTicks > 0)
				return InteractionResult.SUCCESS;
			if (!level.isClientSide()) {
				player.getInventory().placeItemBackInInventory(be.heldBox.copy(), net.minecraft.util.Prediction.SERVER_ONLY);
				AllSoundEvents.playItemPickup(player);
				be.heldBox = ItemStack.EMPTY;
				be.notifyUpdate();
			}
			return InteractionResult.SUCCESS;
		}).consumesAction())
			return InteractionResult.SUCCESS;

		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
		return AllBlockEntities.FLUID_PACKAGER.get();
	}
}
