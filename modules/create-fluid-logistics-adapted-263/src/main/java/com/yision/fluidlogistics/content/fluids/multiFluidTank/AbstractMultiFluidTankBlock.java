package com.yision.fluidlogistics.content.fluids.multiFluidTank;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidHelper.FluidExchange;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.fluids.FluidStack;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler;

public abstract class AbstractMultiFluidTankBlock<T extends AbstractMultiFluidTankBlockEntity<T>>
	extends Block implements IWrenchable, IBE<T> {

	public static final VoxelShape CAMPFIRE_SMOKE_CLIP = Block.box(0, 4, 0, 16, 16, 16);

	protected AbstractMultiFluidTankBlock(Properties properties) {
		super(properties);
	}

	protected abstract String silenceSoundTag();

	@Override
	public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
		return 0;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext context) {
		if (context == CollisionContext.empty())
			return CAMPFIRE_SMOKE_CLIP;
		return state.getShape(level, pos);
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
		return Shapes.block();
	}

	@Override
	public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess scheduledTicks, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
		return state;
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
		if (oldState.getBlock() == state.getBlock())
			return;
		if (moved)
			return;
		withBlockEntityDo(world, pos, AbstractMultiFluidTankBlockEntity::scheduleConnectivityUpdate);
	}
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean moving) {
        AbstractMultiFluidTankBlockEntity<?> removed = REMOVING_TANKS.remove(new RemovalKey(level.dimension(), pos));
        if (removed != null) ConnectivityHandler.splitMulti(removed);
        super.affectNeighborsAfterRemoval(state, level, pos, moving);
    }
    private record RemovalKey(net.minecraft.resources.ResourceKey<Level> dimension, BlockPos pos) {}
    private static final java.util.Map<RemovalKey, AbstractMultiFluidTankBlockEntity<?>> REMOVING_TANKS = new java.util.HashMap<>();
    static void prepareRemoval(AbstractMultiFluidTankBlockEntity<?> tank) {
        if (tank.hasLevel()) REMOVING_TANKS.put(new RemovalKey(tank.getLevel().dimension(), tank.getBlockPos()), tank);
    }


	protected abstract boolean isOwnBlockEntity(BlockEntity be);

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, Direction direction) {
		return getBlockEntityOptional(worldIn, pos).map(AbstractMultiFluidTankBlockEntity::getControllerBE)
			.map(AbstractMultiFluidTankBlockEntity::getComparatorOutput)
			.orElse(0);
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		boolean onClient = level.isClientSide();

		if (stack.isEmpty())
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		if (!player.isCreative())
			return InteractionResult.TRY_WITH_EMPTY_HAND;

		FluidExchange exchange = null;
		T be = ConnectivityHandler.partAt(getBlockEntityType(), level, pos);
		if (be == null)
			return InteractionResult.FAIL;

		IFluidHandler tankCapability = com.yision.fluidlogistics.foundation.capability.LegacyCapabilityAccess.fluid(level, be.getBlockPos(), null);
		if (tankCapability == null)
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		FluidStack prevFluidInTank = tankCapability.getFluidInTank(0).copy();

		if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be))
			exchange = FluidExchange.ITEM_TO_TANK;
		else if (FluidHelper.tryFillItemFromBE(level, player, hand, stack, be))
			exchange = FluidExchange.TANK_TO_ITEM;

		if (exchange == null) {
			if (GenericItemEmptying.canItemBeEmptied(level, stack)
				|| GenericItemFilling.canItemBeFilled(level, stack))
				return InteractionResult.SUCCESS;
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}

		SoundEvent soundevent = null;
		BlockState fluidState = null;
		FluidStack fluidInTank = tankCapability.getFluidInTank(0);

		if (exchange == FluidExchange.ITEM_TO_TANK) {
			net.minecraft.world.level.material.Fluid fluid = fluidInTank.getFluid();
			fluidState = fluid.defaultFluidState().createLegacyBlock();
			soundevent = FluidHelper.getEmptySound(fluidInTank);
		}

		if (exchange == FluidExchange.TANK_TO_ITEM) {
			net.minecraft.world.level.material.Fluid fluid = prevFluidInTank.getFluid();
			fluidState = fluid.defaultFluidState().createLegacyBlock();
			soundevent = FluidHelper.getFillSound(prevFluidInTank);
		}

		if (soundevent != null && !onClient) {
			float pitch = Mth.clamp(
				1 - (1f * fluidInTank.getAmount() / (AbstractMultiFluidTankBlockEntity.getCapacityMultiplier() * 16)),
				0, 1);
			pitch /= 1.5f;
			pitch += .5f;
			pitch += (level.getRandom().nextFloat() - .5f) / 4f;
			level.playSound(null, pos, soundevent, SoundSource.BLOCKS, .5f, pitch);
		}

		if (!FluidStack.isSameFluidSameComponents(fluidInTank, prevFluidInTank)) {
			T controllerBE = be.getControllerBE();
			if (controllerBE != null) {
				if (fluidState != null && onClient) {
					BlockParticleOption blockParticleData = new BlockParticleOption(ParticleTypes.BLOCK, fluidState);
					float fluidLevel = (float) fluidInTank.getAmount() / tankCapability.getTankCapacity(0);

					boolean reversed = fluidInTank.getFluid().getFluidType().isLighterThanAir();
					if (reversed)
						fluidLevel = 1 - fluidLevel;

					Vec3 vec = hitResult.getLocation();
					vec = new Vec3(vec.x,
						controllerBE.getBlockPos().getY() + fluidLevel * particleHeight(controllerBE) + .25f, vec.z);
					Vec3 motion = player.position().subtract(vec).scale(1 / 20f);
					vec = vec.add(motion);
					level.addParticle(blockParticleData, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
					return InteractionResult.SUCCESS;
				}

				controllerBE.sendDataImmediately();
				controllerBE.setChanged();
			}
		}

		return InteractionResult.SUCCESS;
	}

	protected abstract float particleHeight(T controllerBE);

	public static final SoundType SILENCED_METAL = new DeferredSoundType(0.1F, 1.5F, () -> SoundEvents.METAL_BREAK,
		() -> SoundEvents.METAL_STEP,
		() -> SoundEvents.METAL_PLACE, () -> SoundEvents.METAL_HIT, () -> SoundEvents.METAL_FALL);

	@Override
	public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
		SoundType soundType = super.getSoundType(state, world, pos, entity);
		if (entity != null && entity.getPersistentData().contains(silenceSoundTag()))
			return SILENCED_METAL;
		return soundType;
	}

	@Override
	public abstract BlockEntityType<? extends T> getBlockEntityType();
}
