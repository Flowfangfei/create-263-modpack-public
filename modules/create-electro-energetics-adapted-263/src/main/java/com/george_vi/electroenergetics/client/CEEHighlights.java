package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEShapes;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlock;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelClientTicker;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CEEHighlights {
    public static void extract(Camera camera, RenderCollector collector) {
        var mc = Minecraft.getInstance();
        if (mc.level == null
                || !(mc.hitResult instanceof BlockHitResult hit)
                || hit.getType() == HitResult.Type.MISS) return;
        if (WireInteractionHandler.targetedPoint != null) {
            collector.hideBlockOutline = true;
            return;
        }
        var pos = hit.getBlockPos();
        var state = mc.level.getBlockState(pos);
        VoxelShape shape = null;
        if (CEEBlocks.ACCUMULATOR.has(state)
                && !state.getValue(AccumulatorBlock.STACK).isSingle()) {
            Direction facing = state.getValue(AccumulatorBlock.FACING);
            boolean roll = state.getValue(AccumulatorBlock.ROLL);
            boolean first =
                    AccumulatorBlock.clickedOnFirst(
                            roll, facing, hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)));
            shape =
                    first
                            ? (roll
                                    ? CEEShapes.ACCUMULATOR_OUTLINE_RF.get(facing)
                                    : CEEShapes.ACCUMULATOR_OUTLINE_F.get(facing))
                            : (roll
                                    ? CEEShapes.ACCUMULATOR_OUTLINE_R.get(facing)
                                    : CEEShapes.ACCUMULATOR_OUTLINE.get(facing));
        } else if (state.getBlock() instanceof ElectricalPanelBlock)
            shape = ElectricalPanelClientTicker.getHighlightShape(hit, state);
        if (shape == null) return;
        collector.hideBlockOutline = true;
        PoseStack pose = new PoseStack();
        pose.translate(
                pos.getX() - camera.position().x,
                pos.getY() - camera.position().y,
                pos.getZ() - camera.position().z);
        VoxelShape outline = shape;
        float width = mc.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        collector.outline(
                pose,
                (matrices, queue) ->
                        queue.submitShapeOutline(
                                matrices, outline, RenderTypes.lines(), 0x66000000, width, true));
    }
}
