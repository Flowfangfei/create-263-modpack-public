package com.george_vi.electroenergetics.content.electrical_panel;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class ElectricalPanelRenderer extends ElectricalRenderer<ElectricalPanelBlockEntity> {
    public ElectricalPanelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(ElectricalPanelBlockEntity blockEntity, float partialTicks, PoseStack ms,
                              RenderCollector buffer, int light, int overlay) {

        PanelAttachment[] attachments = blockEntity.getAttachments();
        for (PanelAttachment attachment : attachments) {
            if (attachment == null)
                continue;
            ms.pushPose();
            com.george_vi.electroenergetics.client.render.PanelAttachmentRenderers.render(attachment, blockEntity, partialTicks, ms, buffer, light, overlay);
            ms.popPose();
            if (attachment.label != null) {
                ms.pushPose();
                com.george_vi.electroenergetics.client.render.PanelAttachmentRenderers.renderLabel(attachment, blockEntity, partialTicks, ms, buffer, light, overlay);
                ms.popPose();
            }
        }


        float coverAlpha = Mth.lerp(partialTicks, ElectricalPanelClientTicker.prevCoverAlpha, ElectricalPanelClientTicker.coverAlpha);

        if (coverAlpha != 0) {
            PartialModel panelCover = CEEPartialModels.PANEL_COVER;
            if (blockEntity.getBlockState().getBlock() instanceof ElectricalPanelBlock b && b.color != null)
                panelCover = CEEPartialModels.DYED_ELECTRICAL_PANEL_COVERS[b.color.ordinal()];

            ModelBuffers.partial(panelCover, blockEntity.getBlockState())
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
}
