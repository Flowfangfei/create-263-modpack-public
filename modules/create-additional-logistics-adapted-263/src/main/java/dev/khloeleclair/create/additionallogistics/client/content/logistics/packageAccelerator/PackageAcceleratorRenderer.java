package dev.khloeleclair.create.additionallogistics.client.content.logistics.packageAccelerator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CreateVisualizationManager;
import dev.khloeleclair.create.additionallogistics.client.CALRenderModels;
import dev.khloeleclair.create.additionallogistics.common.content.logistics.packageAccelerator.PackageAcceleratorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class PackageAcceleratorRenderer extends KineticBlockEntityRenderer<PackageAcceleratorBlockEntity> {
    public PackageAcceleratorRenderer(BlockEntityRendererProvider.Context context) { super(context); }
    @Override public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!(state instanceof KineticRenderState kinetic) || !(kinetic.blockEntity instanceof PackageAcceleratorBlockEntity be) || isInvalid(be)) return;
        if (CreateVisualizationManager.supportsVisualization(be.getLevel())) return;
        Direction.Axis axis = getRotationAxisOf(be);
        pose.pushPose();
        pose.translate(.5, .5, .5);
        pose.rotate(rotation(axis, getAngleForBe(be, be.getBlockPos(), axis, kinetic.partialTicks)));
        pose.translate(-.5, -.5, -.5);
        CALRenderModels.submit(AllPartialModels.SHAFTLESS_COGWHEEL, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE), true,
            pose, collector, RenderTypes.solidMovingBlock(), state.lightCoords);
        CALRenderModels.submit(AllPartialModels.SHAFT_HALF, be.getBlockState().getValue(FACING).getOpposite(), false,
            pose, collector, RenderTypes.solidMovingBlock(), state.lightCoords);
        pose.popPose();
    }
}
