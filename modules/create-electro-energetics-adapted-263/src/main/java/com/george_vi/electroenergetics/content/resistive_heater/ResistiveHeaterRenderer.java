package com.george_vi.electroenergetics.content.resistive_heater;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.GeometryBuffer;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class ResistiveHeaterRenderer extends ElectricalRenderer<ResistiveHeaterBlockEntity> {
    public ResistiveHeaterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(ResistiveHeaterBlockEntity blockEntity, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {

        float t = blockEntity.smoothHeat.getValue(partialTicks);


        GeometryBuffer heatingElement = ModelBuffers.partial(CEEPartialModels.RESISTIVE_HEATER_HEATING_ELEMENT, blockEntity.getBlockState());
        heatingElement
                .rotateYCenteredDegrees(blockEntity.getBlockState().getValue(ResistiveHeaterBlock.FACING).toYRot())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        if (t > 0.2) {
            int alpha = (int) Mth.clamp(((t - 0.2f)) * 255, 0, 127);
            t /= 3;
            ModelBuffers.partial(CEEPartialModels.RESISTIVE_HEATER_HEATING_ELEMENT_GLOW, blockEntity.getBlockState())
                    .rotateYCenteredDegrees(blockEntity.getBlockState().getValue(ResistiveHeaterBlock.FACING).toYRot())
                    .color(255, (int) (t * 255), (int) (t * 127), alpha)
                    .disableDiffuse()
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.translucentMovingBlock()));
        }
    }
}
