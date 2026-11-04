package com.george_vi.electroenergetics.content.frequency_meter;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class FrequencyMeterRenderer extends ElectricalRenderer<FrequencyMeterBlockEntity> {
    public FrequencyMeterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(FrequencyMeterBlockEntity blockEntity, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {

        float r = blockEntity.smoothFrequency.getValue(partialTicks);

        r *= -6.75f ;

        ModelBuffers.partial(CEEPartialModels.SYNCHROSCOPE_DIAL, blockEntity.getBlockState())
                .light(light)
                .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(FrequencyMeterBlock.FACING).toYRot())
                .translate(0.5f, 0.5f, 7.5f/16f)
                .rotateZDegrees(r + 135)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

    }
}
