package com.george_vi.electroenergetics.content.electric_fan;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricFanBlockEntityRenderer extends ElectricalRenderer<ElectricFanBlockEntity> {
    public ElectricFanBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(ElectricFanBlockEntity blockEntity, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();

        Direction facing = blockEntity.getBlockState().getValue(ElectricFanBlock.FACING);
        ModelBuffers.partial(CEEPartialModels.ELECTRIC_FAN_BLADE, state)
                .light(light)
                .rotateXCenteredDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                .rotateZCenteredDegrees(facing.getAxis().isHorizontal() ? (int) -facing.toYRot() + 180 : 0)
                .rotateYCenteredDegrees(Mth.lerp(partialTicks, blockEntity.prevRotation, blockEntity.rotation))
                .translate(0, state.getValue(ElectricFanBlock.FORWARD) ? 6/16f : 0, 0)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }
}
