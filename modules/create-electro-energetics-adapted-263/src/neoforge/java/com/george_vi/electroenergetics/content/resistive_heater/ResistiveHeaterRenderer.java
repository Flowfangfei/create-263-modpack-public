package com.george_vi.electroenergetics.content.resistive_heater;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import com.george_vi.electroenergetics.CEEPartialModels.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class ResistiveHeaterRenderer extends SmartBlockEntityRenderer<ResistiveHeaterBlockEntity> {
    public ResistiveHeaterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ResistiveHeaterBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        float t = blockEntity.smoothHeat.getValue(partialTicks);


        ModelBuffer heatingElement = Bufferer.partial(CEEPartialModels.RESISTIVE_HEATER_HEATING_ELEMENT, blockEntity.getBlockState());
        heatingElement
                .rotateYCenteredDegrees(blockEntity.getBlockState().getValue(ResistiveHeaterBlock.FACING).toYRot())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        if (t > 0.2) {
            int alpha = (int) Mth.clamp(((t - 0.2f)) * 255, 0, 127);
            t /= 3;
            Bufferer.partial(CEEPartialModels.RESISTIVE_HEATER_HEATING_ELEMENT_GLOW, blockEntity.getBlockState())
                    .rotateYCenteredDegrees(blockEntity.getBlockState().getValue(ResistiveHeaterBlock.FACING).toYRot())
                    .color(255, (int) (t * 255), (int) (t * 127), alpha)
                    .disableDiffuse()
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.translucentMovingBlock()));
        }
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        ResistiveHeaterBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(ResistiveHeaterBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        ResistiveHeaterBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
