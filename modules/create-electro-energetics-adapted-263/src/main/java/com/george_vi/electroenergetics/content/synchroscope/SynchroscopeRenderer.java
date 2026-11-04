package com.george_vi.electroenergetics.content.synchroscope;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class SynchroscopeRenderer extends ElectricalRenderer<SynchroscopeBlockEntity> {
    public SynchroscopeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(SynchroscopeBlockEntity blockEntity, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {
//        float r = AngleHelper.angleLerp(Mth.clamp((blockEntity.counter + partialTicks - 1) / blockEntity.tickLength, 0, 1.1f), blockEntity.prevPhaseOffset, blockEntity.phaseOffset);
        float r = blockEntity.smoothPhase.getValue(partialTicks);
        float r1 = blockEntity.phaseOffset;
//        float r = AngleHelper.angleLerp(partialTicks, blockEntity.prevPhaseOffset, blockEntity.phaseOffset);
        ModelBuffers.partial(CEEPartialModels.SYNCHROSCOPE_DIAL, blockEntity.getBlockState())
                .light(light)
                .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(SynchroscopeBlock.FACING).toYRot())
                .translate(0.5f, 0.5f, 7.5f/16f)
                .rotateZDegrees(r)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
//
//        ModelBuffers.partial(CEEPartialModels.SYNCHROSCOPE_DIAL, blockEntity.getBlockState())
//                .light(light)
//                .rotateYCenteredDegrees(-blockEntity.getBlockState().getValue(SynchroscopeBlock.FACING).toYRot())
//                .translate(0.5f, 0.5f, 7.5f/16f)
//                .color(255, 0, 0, 0)
//                .rotateZDegrees(r1)
//                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

    }
}
