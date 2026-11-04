package com.george_vi.electroenergetics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricalKineticRenderer<T extends KineticBlockEntity> extends ElectricalRenderer<T> {
    public ElectricalKineticRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected BlockState getRenderedBlockState(T be) {
        return be.getBlockState();
    }

    protected GeometryBuffer getRotatedModel(T be, BlockState state) {
        return ModelBuffers.block(state);
    }

    protected RenderType getRenderType(T be, BlockState state) {
        return RenderTypes.solidMovingBlock();
    }

    @Override
    protected void extractModels(
            T be,
            float partialTick,
            PoseStack matrices,
            RenderCollector geometry,
            int light,
            int overlay) {
        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            renderRotatingBuffer(
                    be,
                    getRotatedModel(be, getRenderedBlockState(be)),
                    matrices,
                    geometry.getBuffer(getRenderType(be, be.getBlockState())),
                    light);
        }
    }

    protected void renderRotatingBuffer(
            T be,
            GeometryBuffer model,
            PoseStack matrices,
            RenderCollector.Target target,
            int light) {
        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(be.getBlockState());
        model.rotateCentered(
                        KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), axis),
                        axis.getPositive())
                .light(light)
                .renderInto(matrices, target);
    }
}
