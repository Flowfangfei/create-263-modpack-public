package com.george_vi.electroenergetics.content.railway_electrification.third_rail;

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

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class RailContactShoeRenderer extends ElectricalRenderer<RailContactShoeBlockEntity> {
    public RailContactShoeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(RailContactShoeBlockEntity blockEntity, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();

        float distanceY = Mth.lerp(partialTicks, blockEntity.prevDistanceY, blockEntity.distanceY);
        float yRot = state.getValue(FACING).toYRot();
        if (state.getValue(FACING).getAxis() == Direction.Axis.X)
            yRot += 180;

        ModelBuffers.partial(CEEPartialModels.RAIL_CONTACT_SHOE_CONTACT, state)
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0.5f, -distanceY, 0.5f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

        ModelBuffers.partial(CEEPartialModels.RAIL_CONTACT_SHOE_HINGES, state)
                .center().rotateYDegrees(yRot).uncenter()
                .translate(0.5f, -distanceY, 0.5f)
                .rotateX((float) -Mth.atan2(-distanceY - 0.25f, 0.5f) + Mth.PI)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }
}
