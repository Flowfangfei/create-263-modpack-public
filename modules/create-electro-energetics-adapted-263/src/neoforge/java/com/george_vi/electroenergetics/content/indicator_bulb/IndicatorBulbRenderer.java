package com.george_vi.electroenergetics.content.indicator_bulb;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.bulb.BulbBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.data.Iterate;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class IndicatorBulbRenderer extends SmartBlockEntityRenderer<IndicatorBulbBlockEntity> {
    public IndicatorBulbRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(IndicatorBulbBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        int side = state.getValue(IndicatorBulbBlock.SIDE);
        TransformStack<PoseTransformStack> msr = TransformStack.of(ms);

        for (boolean opposite : Iterate.falseAndTrue) {
            if (((side + 1) & (opposite ? 2 : 1)) == 0)
                continue;
            float lightStrength = opposite ? blockEntity.secondSmoothLight.getValue(partialTicks) : blockEntity.firstSmoothLight.getValue(partialTicks);
            DyeColor color = opposite ? blockEntity.secondColor : blockEntity.firstColor;
            ms.pushPose();
            msr.center()
                    .rotateYDegrees(state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(BulbBlock.FACING).toYRot() : 0)
                    .rotateXDegrees(state.getValue(BulbBlock.FACING) == Direction.DOWN ? 180 : state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                    .rotateZDegrees(state.getValue(BulbBlock.FACING).getAxis() == Direction.Axis.Z ? 180 : 0)
                    .rotateYDegrees((state.getValue(BulbBlock.ROLL) ? 90 : 0) + (opposite ^ state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 180 : 0))
                    .uncenter();

            Bufferer.partial(CEEPartialModels.INDICATOR_BULB_TUBE, state)
                    .color(color.getTextColor())
                    .light(light)
                    .disableDiffuse()
                    .renderInto(ms, buffer.getBuffer(lightStrength > 0.05f ? com.simibubi.create.foundation.render.RenderTypes.additive() : RenderTypes.translucentMovingBlock()));


            if (lightStrength > 0.05f) {
                float factor = lightStrength;
                int newColor = ((int)(((color.getMapColor().col >> 16) & 0xFF) * factor) << 16)
                        | ((int)(((color.getMapColor().col >> 8) & 0xFF) * factor) << 8)
                        | ((int)((color.getMapColor().col & 0xFF) * factor));

                Bufferer.partial(CEEPartialModels.INDICATOR_BULB_GLOW, state)
                        .color(newColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4/16f, 5/16f, 8/16f)
                        .scale(2, 5, 2)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.RenderTypes.additive()));

                int cubeColor = ((int)(((0xffffff >> 16) & 0xFF) * factor) << 16)
                        | ((int)(((0xffffff >> 8) & 0xFF) * factor) << 8)
                        | ((int)((0xffffff & 0xFF) * factor));

                Bufferer.partial(CEEPartialModels.INDICATOR_BULB_CUBE, state)
                        .color(cubeColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4/16f, 5/16f, 8/16f)
                        .scale(1f, 4.3f, 1f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.RenderTypes.additive()));
            }

            ms.popPose();
        }
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        IndicatorBulbBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(IndicatorBulbBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        IndicatorBulbBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
