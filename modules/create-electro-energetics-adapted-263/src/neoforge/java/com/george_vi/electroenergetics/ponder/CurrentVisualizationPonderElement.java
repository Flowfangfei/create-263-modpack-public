package com.george_vi.electroenergetics.ponder;

import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.math.VecHelper;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.ponder.api.client.level.PonderLevel;
import net.createmod.ponder.impl.client.element.AnimatedSceneElementBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.LightCoordsUtil;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CurrentVisualizationPonderElement extends AnimatedSceneElementBase {
    final InWorldNode node1, node2;
    final float speed;
    final float sag;
    final float offset;
    final boolean valid;

    public CurrentVisualizationPonderElement(InWorldNode node1, InWorldNode node2, float speed, float sag, float offset, boolean valid) {
        this.node1 = node1;
        this.node2 = node2;
        this.speed = speed;
        this.sag = sag;
        this.offset = offset;
        this.valid = valid;
    }

    @Override
    protected void renderLast(PonderLevel world, MultiBufferSource ignored, net.minecraft.client.renderer.SubmitNodeCollector queue, net.minecraft.client.Camera camera, net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState, com.mojang.blaze3d.vertex.PoseStack pose, float fade, float pt) {
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(queue);
        collect(world,geometry,pose,fade,pt);
        geometry.submit();
    }
    private void collect(PonderLevel world, MultiBufferSource buffer, com.mojang.blaze3d.vertex.PoseStack pose, float fade, float pt) {
        Vec3 pos1 = node1.getPosition(world);
        Vec3 pos2 = node2.getPosition(world);

        if (pos1 == null)
            pos1 = Vec3.atCenterOf(node1.sourcePos());
        if (pos2 == null)
            pos2 = Vec3.atCenterOf(node2.sourcePos());

        float progress = speed == 0 ? 0 : (AnimationTickHolder.getRenderTime() % (1 / (speed / 10)) + offset) * (speed / 10);

        if (speed < 0)
            progress = 1 - Math.abs(progress);
        List<Vec3> points = QuadraticWireHelper.cablePointsRaw(pos1, pos2, sag);
        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            Vec3 position = VecHelper.lerp(progress, point, nextPoint);

            Bufferer.block(valid ? Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.YELLOW).defaultBlockState() : Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.RED).defaultBlockState())
                    .translate(position)
                    .scale(0.1875f + 0.0001f)
                    .uncenter()
                    .disableDiffuse()
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        }
    }
}
