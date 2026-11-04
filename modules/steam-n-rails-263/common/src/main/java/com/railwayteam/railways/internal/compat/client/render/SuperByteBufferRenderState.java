package com.railwayteam.railways.internal.compat.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Immutable per-submission snapshot; pose data is copied before the renderer defers geometry. */
public final class SuperByteBufferRenderState {
    private final SuperByteBuffer buffer;
    SuperByteBufferRenderState(SuperByteBuffer buffer) { this.buffer=buffer; }
    public void submit(PoseStack poses, SubmitNodeCollector collector) { submit(RenderTypes.cutoutMovingBlock(),poses,collector); }
    public void submit(RenderType type, PoseStack poses, SubmitNodeCollector collector) {
        collector.submitCustomGeometry(poses,type,(root,consumer)-> { var local=pose(root);buffer.renderInto(local,consumer); });
    }
    public void submit(RenderType type, PoseStack poses, OrderedSubmitNodeCollector collector) {
        collector.submitCustomGeometry(poses,type,(root,consumer)-> { var local=pose(root);buffer.renderInto(local,consumer); });
    }
    public void submit(PoseStack.Pose transform, PoseStack poses, SubmitNodeCollector collector) {
        poses.pushPose();poses.last().pose().mul(transform.pose());poses.last().normal().mul(transform.normal());
        try { submit(poses,collector); } finally { poses.popPose(); }
    }
    public void renderInto(PoseStack.Pose root, com.mojang.blaze3d.vertex.VertexConsumer consumer) { buffer.renderInto(pose(root),consumer); }
    private static PoseStack pose(PoseStack.Pose root) {
        var local=new PoseStack();local.last().pose().set(root.pose());local.last().normal().set(root.normal());return local;
    }
}
