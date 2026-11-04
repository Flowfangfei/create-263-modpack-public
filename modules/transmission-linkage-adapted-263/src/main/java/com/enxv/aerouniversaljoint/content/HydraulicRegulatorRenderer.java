package com.enxv.aerouniversaljoint.content;

import com.mojang.blaze3d.vertex.PoseStack;

import com.simibubi.create.AllPartialModels;
import com.enxv.aerouniversaljoint.client.render.FrozenKineticBlockEntityRenderer;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import dev.simulated_team.simulated.compat.render.CachedBuffers;
import dev.simulated_team.simulated.compat.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class HydraulicRegulatorRenderer extends FrozenKineticBlockEntityRenderer<HydraulicRegulatorBlockEntity> {
    public HydraulicRegulatorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HydraulicRegulatorBlockEntity be, float partialTicks, PoseStack ms, SubmitNodeCollector buffer,
                              int light, int overlay) {
        BlockState state = be.getBlockState();
        SubmitNodeCollector cutoutBuffer = buffer;
        Direction.Axis axis = HydraulicRegulatorBlock.getStressAxis(state);
        renderShaftHalf(be, state, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE), ms, cutoutBuffer, light);
        renderShaftHalf(be, state, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE), ms, cutoutBuffer, light);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(HydraulicRegulatorBlockEntity be, BlockState state) {
        return CachedBuffers.partial(AllPartialModels.SHAFT_HALF, KineticBlockEntityRenderer.shaft(HydraulicRegulatorBlock.getStressAxis(state)));
    }

    private void renderShaftHalf(HydraulicRegulatorBlockEntity be, BlockState state, Direction facing,
                                 PoseStack ms, SubmitNodeCollector buffer, int light) {
        renderRotatingBuffer(be, CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, facing), ms, buffer, light);
    }
}
