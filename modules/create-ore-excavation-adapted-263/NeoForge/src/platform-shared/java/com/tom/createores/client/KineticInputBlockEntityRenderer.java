package com.tom.createores.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.tom.createores.block.KineticInputBlock;
import com.tom.createores.block.entity.KineticInputBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Native renderer remains visible with either Flywheel backend. */
public class KineticInputBlockEntityRenderer implements BlockEntityRenderer<KineticInputBlockEntity, KineticInputBlockEntityRenderer.State> {
    public KineticInputBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}
    public State createRenderState() { return new State(); }
    public void extractRenderState(KineticInputBlockEntity be, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(be, state, overlay);
        state.direction = be.getBlockState().getValue(KineticInputBlock.SHAFT_FACING);
        state.angle = KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), state.direction.getAxis(), partialTick);
    }
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        var model = AllPartialModels.SHAFT_HALF.get();
        if (model == null) return;
        pose.pushPose(); pose.translate(.5, .5, .5);
        pose.rotate(new Quaternionf().rotationTo(new Vector3f(0, 0, 1), new Vector3f(state.direction.getStepX(), state.direction.getStepY(), state.direction.getStepZ())));
        pose.rotate(com.mojang.math.Axis.ZP.rotation(state.angle)); pose.translate(-.5, -.5, -.5);
        collector.submitBlockModel(pose, RenderTypes.cutoutMovingBlock(), java.util.List.of(model), BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
    public static class State extends BlockEntityRenderState { private Direction direction = Direction.SOUTH; private float angle; }
}
