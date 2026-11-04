package com.george_vi.electroenergetics.content.bulb;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.minecraft.util.LightCoordsUtil;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BulbBlockEntityRenderer extends SmartBlockEntityRenderer<BulbBlockEntity> {
    public BulbBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BulbBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        PoseTransformStack transformStack = TransformStack.of(ms);
        transformStack.center()
                .rotateYDegrees(state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(BulbBlock.FACING).toYRot() : 0)
                .rotateXDegrees(state.getValue(BulbBlock.FACING) == Direction.DOWN ? 180 : state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                .rotateZDegrees(state.getValue(BulbBlock.FACING).getAxis() == Direction.Axis.Z ? 180 : 0)
                .rotateYDegrees(state.getValue(BulbBlock.ROLL) ? 90 : 0)
                .uncenter();

        if (CEEBlocks.BULB.has(state)) {
            float bulbLight = blockEntity.smoothLight.getValue(partialTicks);
            if (bulbLight > 0.5) {
                float factor = (bulbLight - 0.3f);
                int newColor = ((int)(255 * factor) << 16)
                        | ((int)(200 * factor) << 8)
                        | ((int)(140 * factor));
                if (bulbLight < 0.65)
                    Bufferer.partial(CEEPartialModels.BULB_GLASS, state)
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));

                Bufferer.partial(CEEPartialModels.BULB_FILAMENT_BRIGHT, state)
                        .color(newColor)
                        .disableDiffuse()
                        .renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.RenderTypes.additive()));

                Bufferer.partial(CEEPartialModels.INDICATOR_BULB_GLOW, state)
                        .light(0xf000f0)
                        .color(newColor)
                        .disableDiffuse()
                        .translate(8/16f, 7/16f, 8/16f)
                        .scale(3.5f, 3.5f, 3.5f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.RenderTypes.additive()));

                Bufferer.partial(CEEPartialModels.INDICATOR_BULB_GLOW, state)
                        .light(0xf000f0)
                        .color(newColor)
                        .disableDiffuse()
                        .translate(8/16f, 7/16f, 8/16f)
                        .scale(3.0625f, 3.0625f, 3.0625f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.RenderTypes.additive()));
            } else {
                Bufferer.partial(CEEPartialModels.BULB_FILAMENT, state)
                        .light(bulbLight > 0.05 ? LightCoordsUtil.FULL_BRIGHT : light)
                        .color(bulbLight > 0.7 ? 255 : (int) (bulbLight * 330) + 22, bulbLight > 0.7 ? 255 : (int) (bulbLight * 330) + 22, bulbLight > 0.7 ? 255 : (int) (bulbLight * 330) + 22, 255)
                        .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));
                Bufferer.partial(CEEPartialModels.BULB_GLASS, state)
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));
            }
        } else {
            Bufferer.partial(CEEPartialModels.BULB_BROKEN_FILAMENT, state)
                    .light(light)
                    .color(22, 22, 22, 255)
                    .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));
            Bufferer.partial(CEEPartialModels.BULB_GLASS, state)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));
        }
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        BulbBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(BulbBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        BulbBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
