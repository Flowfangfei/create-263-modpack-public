package com.george_vi.electroenergetics.ponder;

import net.minecraft.world.item.DyeColor;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.foundation.QuadraticWireHelper;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.math.VecHelper;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.element.AnimatedSceneElementBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
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
    protected void renderLast(EntityRenderDispatcher entities,ItemModelResolver items,PonderLevel world,SubmitNodeCollector queue,CameraRenderState camera,PoseStack ms,float fade,float pt) {
        RenderCollector buffer=new RenderCollector();
        collect(world,buffer,ms,fade,pt);
        buffer.submit(new PoseStack(),queue);
    }
    private void collect(PonderLevel world,RenderCollector buffer,PoseStack ms,float fade,float pt) {
        Vec3 pos1 = node1.getPosition(world);
        Vec3 pos2 = node2.getPosition(world);

        if (pos1 == null)
            pos1 = Vec3.atCenterOf(node1.sourcePos());
        if (pos2 == null)
            pos2 = Vec3.atCenterOf(node2.sourcePos());

        float progress = speed == 0 ? 0 : (AnimationTickHolder.getRenderTime(world) % (1 / (speed / 10)) + offset) * (speed / 10);

        if (speed < 0)
            progress = 1 - Math.abs(progress);
        List<Vec3> points = QuadraticWireHelper.cablePointsRaw(pos1, pos2, sag);
        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);
            Vec3 nextPoint = i == points.size() - 1 ? pos2 : points.get(i + 1);
            Vec3 position = VecHelper.lerp(progress, point, nextPoint);

            ModelBuffers.block(valid ? Blocks.CONCRETE.pick(DyeColor.YELLOW).defaultBlockState() : Blocks.CONCRETE.pick(DyeColor.RED).defaultBlockState())
                    .translate(position)
                    .scale(0.1875f + 0.0001f)
                    .uncenter()
                    .disableDiffuse()
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        }
    }
}
