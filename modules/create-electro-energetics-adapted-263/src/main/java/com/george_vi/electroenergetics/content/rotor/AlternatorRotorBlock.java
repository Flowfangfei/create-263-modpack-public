package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.CEEBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

public class AlternatorRotorBlock extends RotatedPillarKineticBlock implements IBE<AlternatorRotorBlockEntity> {
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public AlternatorRotorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.CRUSHING_WHEEL_COLLISION_SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult)
                        .placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   net.minecraft.world.level.redstone.Orientation neighborPos, boolean movedByPiston) {

        Direction.Axis axis = state.getValue(AlternatorRotorBlock.AXIS);

        // Pass the update only when updated by a stator
        for (Direction direction : Iterate.directions) {
            if (direction.getAxis() == axis)
                continue;
            level.neighborChanged(pos.relative(direction), state.getBlock(), null);
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Class<AlternatorRotorBlockEntity> getBlockEntityClass() {
        return AlternatorRotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AlternatorRotorBlockEntity> getBlockEntityType() {
        return CEEBlockEntityTypes.ALTERNATOR_ROTOR.get();
    }

    private static class PlacementHelper implements IPlacementHelper {

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return CEEBlocks.STATOR::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return CEEBlocks.ALTERNATOR_ROTOR::has;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos,
                                         BlockHitResult ray) {
            Direction.Axis rotorAxis = state.getValue(AXIS);
            for (Direction direction : Iterate.directions) {
                if (direction.getAxis() == rotorAxis)
                    continue;
                BlockPos targetPos = pos.relative(direction);
                if (level.getBlockState(targetPos).canBeReplaced()) {

                    Direction facing = direction.getOpposite();
                    boolean roll = rotorAxis.isVertical() ? false :
                            facing.getAxis().isHorizontal() ? true :
                            rotorAxis == Direction.Axis.X;
                    return PlacementOffset.success(targetPos)
                            .withTransform(bs ->
                                    bs.setValue(StatorBlock.ROLL, roll)
                                    .setValue(StatorBlock.FACING, facing));
                }
            }

            return PlacementOffset.fail();
        }
    }
}
