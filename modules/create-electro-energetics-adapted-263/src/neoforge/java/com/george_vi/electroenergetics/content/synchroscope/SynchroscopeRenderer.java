package com.george_vi.electroenergetics.content.synchroscope;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.api.math.AngleHelper;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class SynchroscopeRenderer extends SmartBlockEntityRenderer<SynchroscopeBlockEntity> {
    public SynchroscopeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SynchroscopeBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
//        float r = AngleHelper.angleLerp(Mth.clamp((blockEntity.counter + partialTicks - 1) / blockEntity.tickLength, 0, 1.1f), blockEntity.prevPhaseOffset, blockEntity.phaseOffset);
        float r = blockEntity.smoothPhase.getValue(partialTicks);
        float r1 = blockEntity.phaseOffset;
//        float r = AngleHelper.angleLerp(partialTicks, blockEntity.prevPhaseOffset, blockEntity.phaseOffset);
        Bufferer.partial(CEEPartialModels.SYNCHROSCOPE_DIAL, blockEntity.getBlockState())
                .light(light)
                .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(SynchroscopeBlock.FACING).toYRot())
                .translate(0.5f, 0.5f, 7.5f/16f)
                .rotateZDegrees(r)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
//
//        Bufferer.partial(CEEPartialModels.SYNCHROSCOPE_DIAL, blockEntity.getBlockState())
//                .light(light)
//                .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(SynchroscopeBlock.FACING).toYRot())
//                .translate(0.5f, 0.5f, 7.5f/16f)
//                .color(255, 0, 0, 0)
//                .rotateZDegrees(r1)
//                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        SynchroscopeBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(SynchroscopeBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        SynchroscopeBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
