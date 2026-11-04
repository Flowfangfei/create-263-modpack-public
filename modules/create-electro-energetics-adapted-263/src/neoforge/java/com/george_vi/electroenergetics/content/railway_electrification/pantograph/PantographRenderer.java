package com.george_vi.electroenergetics.content.railway_electrification.pantograph;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PantographRenderer extends SmartBlockEntityRenderer<PantographBlockEntity> {
    public PantographRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PantographBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        float extensionState = Mth.lerp(partialTicks, blockEntity.prevExtensionState, blockEntity.currentExtensionState);

        int color = blockEntity.color.getTextureDiffuseColor();

        float yRot = state.getValue(FACING).toYRot();
        if (state.getValue(FACING).getAxis() == Direction.Axis.Z)
            yRot += 180;

        if (state.getValue(PantographBlock.DOUBLE)) {
            float rotationFactor = 27;
            Bufferer.partial(CEEPartialModels.PANTOGRAPH_BASE_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARMS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.5)
                    .rotateXDegrees(-90 + extensionState * rotationFactor)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            float lowerArmRadians = (-90 + extensionState * rotationFactor) * Mth.DEG_TO_RAD;
            double armHingePosY = Mth.cos(lowerArmRadians) * 1.5;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.5;


            float b = (float) (-0.4f - Math.abs(armHingePosX));
            float a = Mth.sqrt(-b * b + 2f * 2f);


            Bufferer.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARMS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.5)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateX((float) Mth.atan2(a, b) - Mth.HALF_PI)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.5625 + a + armHingePosY, 1)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            float springHingePosY = Mth.cos(lowerArmRadians + 0.5f) * 0.425f;
            float springHingePosX = Mth.sin(lowerArmRadians + 0.5f) * 0.425f;

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_SPRINGS_DOUBLE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, springHingePosY + 0.375, springHingePosX + 0.5f)
                    .scaleZ((-springHingePosX + 0.5f) * (16/13f))
                    .rotateXDegrees(0)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

        } else {
            Bufferer.partial(CEEPartialModels.PANTOGRAPH_BASE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .rotateXDegrees(-75 + extensionState * 30)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
            float lowerArmRadians = (-75 + extensionState * 30) * Mth.DEG_TO_RAD;

            double armHingePosY = Mth.cos(lowerArmRadians) * 1.875;
            double armHingePosX = Mth.sin(lowerArmRadians) * 1.875;

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(12/16f, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .rotateYDegrees(12.5f)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(4/16f, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .rotateXDegrees(-1 - extensionState * 50)
                    .rotateYDegrees(-12.5f)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            float upperArmRadians = (89 - extensionState * 50) * Mth.DEG_TO_RAD;
            Bufferer.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.8125)
                    .translate(0, armHingePosY, armHingePosX)
                    .translate(0, Mth.cos(upperArmRadians) * 1.9, Mth.sin(upperArmRadians) * 1.9)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_SPRINGS, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.25)
                    .rotateXDegrees(-22 + extensionState * -20)
                    .scale(1, 1, 1 + extensionState / 2)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            Bufferer.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_ROD, state)
                    .center().rotateYDegrees(yRot).uncenter()
                    .translate(0, 0.375, 0.1875)
                    .rotateXDegrees(-77 + extensionState * 43)
                    .color(color)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
        }
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        PantographBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(PantographBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        PantographBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
