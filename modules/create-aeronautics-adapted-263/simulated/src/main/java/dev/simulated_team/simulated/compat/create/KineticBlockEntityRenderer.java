package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.compat.render.CachedBuffers;
import dev.simulated_team.simulated.compat.render.SuperByteBuffer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class KineticBlockEntityRenderer<T extends KineticBlockEntity> extends SmartBlockEntityRenderer<T> {
    public KineticBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected boolean shouldSkipLegacyRender(final T blockEntity) {
        return false;
    }

    @Override
    protected void renderSafe(final T blockEntity, final float partialTicks, final PoseStack poseStack,
                              final net.minecraft.client.renderer.SubmitNodeCollector bufferSource, final int light, final int overlay) {
        final BlockState state = getRenderedBlockState(blockEntity);
        renderRotatingBuffer(blockEntity, getRotatedModel(blockEntity, state), poseStack, bufferSource, light);
    }

    protected SuperByteBuffer getRotatedModel(final T blockEntity, final BlockState state) {
        return CachedBuffers.block(net.createmod.catnip.api.client.render.CachedBuffers.GENERIC_BLOCK, state);
    }

    protected BlockState getRenderedBlockState(final T blockEntity) {
        return blockEntity.getBlockState();
    }

    protected void renderRotatingBuffer(final T blockEntity, final SuperByteBuffer buffer, final PoseStack poseStack,
                                        final SubmitNodeCollector collector, final int light) {
        kineticRotationTransform(buffer, blockEntity, getRotationAxisOf(blockEntity),
                getAngleForBe(blockEntity, blockEntity.getBlockPos(), getRotationAxisOf(blockEntity)), light)
                .submit(poseStack, collector);
    }

    public static Direction.Axis getRotationAxisOf(final KineticBlockEntity blockEntity) {
        return com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
    }

    public static float getAngleForBe(final KineticBlockEntity blockEntity, final BlockPos pos, final Direction.Axis axis) {
        return com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe(blockEntity, pos, axis);
    }

    public static float getRotationOffsetForPosition(final KineticBlockEntity blockEntity, final BlockPos pos, final Direction.Axis axis) {
        return com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getRotationOffsetForPosition(blockEntity, pos, axis);
    }

    public static BlockState shaft(final Direction.Axis axis) {
        return com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.shaft(axis);
    }

    public static SuperByteBuffer kineticRotationTransform(final SuperByteBuffer buffer, final KineticBlockEntity blockEntity,
                                                           final Direction.Axis axis, final float angle, final int light) {
        return buffer.light(light).color(getColor(blockEntity)).rotateCentered(angle, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
    }

    public static void renderRotatingKineticBlock(final KineticBlockEntity blockEntity, final BlockState state,
                                                  final PoseStack poseStack, final SubmitNodeCollector collector, final int light) {
        kineticRotationTransform(CachedBuffers.block(net.createmod.catnip.api.client.render.CachedBuffers.GENERIC_BLOCK, state),
                blockEntity, getRotationAxisOf(blockEntity), getAngleForBe(blockEntity, blockEntity.getBlockPos(), getRotationAxisOf(blockEntity)), light)
                .submit(poseStack, collector);
    }
    public static net.createmod.catnip.api.theme.Color getColor(KineticBlockEntity be){
        var effects=((dev.simulated_team.simulated.mixin.accessor.KineticEffectsAccessor)be).simulated$getEffects();
        float effect=effects==null?0:((dev.simulated_team.simulated.mixin.accessor.KineticEffectValueAccessor)effects).simulated$getOverstress();
        var white=net.createmod.catnip.api.theme.Color.WHITE;
        return effect==0?white:white.mixWith(effect>0?net.createmod.catnip.api.theme.Color.RED:net.createmod.catnip.api.theme.Color.SPRING_GREEN,Math.abs(effect));
    }
    public static float getAngleForBe(KineticBlockEntity be,BlockPos pos,Direction.Axis axis,float partialTick){
        return com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe(be,pos,axis,partialTick);
    }
}
