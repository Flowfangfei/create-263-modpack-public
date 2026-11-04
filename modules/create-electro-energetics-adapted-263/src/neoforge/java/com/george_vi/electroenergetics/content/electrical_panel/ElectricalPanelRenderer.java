package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class ElectricalPanelRenderer extends SmartBlockEntityRenderer<ElectricalPanelBlockEntity> {
    public ElectricalPanelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ElectricalPanelBlockEntity blockEntity, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        PanelAttachment[] attachments = blockEntity.getAttachments();
        for (PanelAttachment attachment : attachments) {
            if (attachment == null)
                continue;
            ms.pushPose();
            attachment.render(blockEntity, partialTicks, ms, buffer, light, overlay);
            ms.popPose();
            if (attachment.label != null) {
                ms.pushPose();
                attachment.renderLabel(blockEntity, partialTicks, ms, buffer, light, overlay);
                ms.popPose();
            }
        }


        float coverAlpha = Mth.lerp(partialTicks, ElectricalPanelClientTicker.prevCoverAlpha, ElectricalPanelClientTicker.coverAlpha);

        if (coverAlpha != 0) {
            PartialModel panelCover = CEEPartialModels.PANEL_COVER;
            if (blockEntity.getBlockState().getBlock() instanceof ElectricalPanelBlock b && b.color != null)
                panelCover = CEEPartialModels.DYED_ELECTRICAL_PANEL_COVERS[b.color.ordinal()];

            Bufferer.partial(panelCover, blockEntity.getBlockState())
                    .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(ElectricalPanelBlock.FACING).toYRot() + 180)
                    .color(255, 255, 255, (int) (coverAlpha * 255))
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(coverAlpha == 1 ? RenderTypes.solidMovingBlock() : RenderTypes.translucentMovingBlock()));
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        ElectricalPanelBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(ElectricalPanelBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        ElectricalPanelBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
