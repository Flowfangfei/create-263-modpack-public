package com.george_vi.electroenergetics.content.variac;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.george_vi.electroenergetics.client.render.ElectricalKineticRenderer;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.GeometryBuffer;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class VariacRenderer extends ElectricalKineticRenderer<VariacBlockEntity> {

    public VariacRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(VariacBlockEntity be, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {

        BlockState state = getRenderedBlockState(be);
        RenderType type = getRenderType(be, state);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        int rot = 0;

        Direction facing = state.getValue(VariacBlock.HORIZONTAL_FACING);

        if (facing == Direction.NORTH)
            rot = -135;
        else if (facing == Direction.SOUTH)
            rot = 45;
        else if (facing == Direction.EAST)
            rot = 135;
        else if (facing == Direction.WEST)
            rot = -45;

        ModelBuffers.partial(CEEPartialModels.VARIAC_DIAL, state)
                .center()
                .rotateYDegrees(be.smoothProgress.getValue(partialTicks) * 270 + rot)
                .uncenter()
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    @Override
    protected GeometryBuffer getRotatedModel(VariacBlockEntity be, BlockState state) {
        return ModelBuffers.partialFacing(CEEPartialModels.VARIAC_SHAFT, state, Direction.WEST);
    }
}
