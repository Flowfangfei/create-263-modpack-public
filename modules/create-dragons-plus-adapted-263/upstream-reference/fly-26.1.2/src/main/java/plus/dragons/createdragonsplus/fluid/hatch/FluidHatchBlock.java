/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.fluid.hatch;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.spout.FillingBySpout;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import plus.dragons.createdragonsplus.registry.CDPBlockEntities;

public final class FluidHatchBlock extends HorizontalDirectionalBlock
        implements IBE<FluidHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {
    public static final MapCodec<FluidHatchBlock> CODEC = simpleCodec(FluidHatchBlock::new);

    public FluidHatchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, WATERLOGGED));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || context.getClickedFace().getAxis().isVertical()) {
            return null;
        }
        return withWater(state.setValue(FACING, context.getClickedFace().getOpposite()), context);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random) {
        updateWater(level, tickAccess, state, pos);
        return state;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (FakePlayerHandler.has(player)) {
            return InteractionResult.SUCCESS;
        }

        Direction facing = state.getValue(FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockState targetState = level.getBlockState(targetPos);
        BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
        Storage<FluidVariant> tank = FluidStorage.SIDED.find(
                level, targetPos, targetState, targetBlockEntity, facing.getOpposite());
        if (tank == null) {
            return InteractionResult.FAIL;
        }
        ContainerItemContext itemContext = ContainerItemContext.forPlayerInteraction(player, hand);
        Storage<FluidVariant> item = itemContext.find(FluidStorage.ITEM);
        ServerFilteringBehaviour filter = BlockEntityBehaviour.get(level, pos, ServerFilteringBehaviour.TYPE);
        if (filter == null) {
            return InteractionResult.FAIL;
        }

        Optional<Transfer> transfer;
        if (player.isShiftKeyDown()) {
            transfer = item == null ? Optional.empty() : move(tank, item, filter, true);
            if (transfer.isEmpty()) {
                transfer = moveTankToGenericItem((ServerLevel) level, tank, stack, player, hand, filter);
            }
            if (transfer.isEmpty()) {
                transfer = item == null ? Optional.empty() : move(item, tank, filter, false);
            }
            if (transfer.isEmpty()) {
                transfer = moveGenericItemToTank((ServerLevel) level, stack, player, hand, tank, filter);
            }
        } else {
            transfer = item == null ? Optional.empty() : move(item, tank, filter, false);
            if (transfer.isEmpty()) {
                transfer = moveGenericItemToTank((ServerLevel) level, stack, player, hand, tank, filter);
            }
            if (transfer.isEmpty()) {
                transfer = item == null ? Optional.empty() : move(tank, item, filter, true);
            }
            if (transfer.isEmpty()) {
                transfer = moveTankToGenericItem((ServerLevel) level, tank, stack, player, hand, filter);
            }
        }
        if (transfer.isEmpty()) {
            return InteractionResult.SUCCESS;
        }

        if (targetBlockEntity != null) {
            targetBlockEntity.setChanged();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.getChunkSource().blockChanged(targetPos);
            }
        }
        level.playSound(
                null,
                pos,
                transfer.get().tankToItem() ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY,
                SoundSource.BLOCKS,
                0.5F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
        return InteractionResult.SUCCESS;
    }

    private static Optional<Transfer> moveGenericItemToTank(
            ServerLevel level,
            ItemStack held,
            Player player,
            InteractionHand hand,
            Storage<FluidVariant> tank,
            ServerFilteringBehaviour filter) {
        if (!GenericItemEmptying.canItemBeEmptied(level, held)) {
            return Optional.empty();
        }
        var simulated = GenericItemEmptying.emptyItem(level, held.copy(), true);
        FluidStack fluid = simulated.getFirst();
        if (fluid.isEmpty() || fluid.getAmount() <= 0 || !filter.test(fluid)) {
            return Optional.empty();
        }
        FluidVariant variant = FluidVariant.of(fluid.getFluid(), fluid.getComponentChanges());
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = tank.insert(variant, fluid.getAmount(), transaction);
            if (inserted != fluid.getAmount()) {
                return Optional.empty();
            }
            transaction.commit();
        }
        var result = GenericItemEmptying.emptyItem(level, held, false);
        finishContainerExchange(player, hand, held, result.getSecond());
        return Optional.of(new Transfer(fluid.getAmount(), false));
    }

    private static Optional<Transfer> moveTankToGenericItem(
            ServerLevel level,
            Storage<FluidVariant> tank,
            ItemStack held,
            Player player,
            InteractionHand hand,
            ServerFilteringBehaviour filter) {
        if (!FillingBySpout.canItemBeFilled(level, held)) {
            return Optional.empty();
        }
        for (StorageView<FluidVariant> view : tank) {
            if (view.isResourceBlank() || view.getAmount() <= 0) {
                continue;
            }
            FluidVariant variant = view.getResource();
            int availableAmount = (int) Math.min(Integer.MAX_VALUE, view.getAmount());
            FluidStack available = new FluidStack(variant.getFluid(), availableAmount, variant.getComponentsPatch());
            if (!filter.test(available)) {
                continue;
            }
            int required = FillingBySpout.getRequiredAmountForItem(level, held.copyWithCount(1), available.copy());
            if (required <= 0 || required > view.getAmount()) {
                continue;
            }
            FluidStack simulatedFluid = available.copy();
            ItemStack simulatedItem = held.copy();
            ItemStack simulatedResult = FillingBySpout.fillItem(level, required, simulatedItem, simulatedFluid);
            if (simulatedResult.isEmpty()) {
                continue;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                if (tank.extract(variant, required, transaction) != required) {
                    continue;
                }
                transaction.commit();
            }
            ItemStack result = FillingBySpout.fillItem(level, required, held, available);
            finishContainerExchange(player, hand, held, result);
            return Optional.of(new Transfer(required, true));
        }
        return Optional.empty();
    }

    private static void finishContainerExchange(
            Player player, InteractionHand hand, ItemStack consumedStack, ItemStack result) {
        if (result.isEmpty()) {
            return;
        }
        if (consumedStack.isEmpty()) {
            player.setItemInHand(hand, result);
        } else {
            player.getInventory().placeItemBackInInventory(result);
        }
    }

    private static Optional<Transfer> move(
            Storage<FluidVariant> from,
            Storage<FluidVariant> to,
            ServerFilteringBehaviour filter,
            boolean tankToItem) {
        for (StorageView<FluidVariant> view : from) {
            if (view.isResourceBlank() || view.getAmount() <= 0) {
                continue;
            }
            FluidVariant variant = view.getResource();
            int amount = (int) Math.min(Integer.MAX_VALUE, view.getAmount());
            if (!filter.test(new FluidStack(variant.getFluid(), amount, variant.getComponentsPatch()))) {
                continue;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = to.insert(variant, amount, transaction);
                if (inserted <= 0 || from.extract(variant, inserted, transaction) != inserted) {
                    continue;
                }
                transaction.commit();
                return Optional.of(new Transfer(inserted, tankToItem));
            }
        }
        return Optional.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.ITEM_HATCH.get(state.getValue(FACING).getOpposite());
    }

    @Override
    public Class<FluidHatchBlockEntity> getBlockEntityClass() {
        return FluidHatchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidHatchBlockEntity> getBlockEntityType() {
        return CDPBlockEntities.FLUID_HATCH.get();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    private record Transfer(long amount, boolean tankToItem) {}
}
