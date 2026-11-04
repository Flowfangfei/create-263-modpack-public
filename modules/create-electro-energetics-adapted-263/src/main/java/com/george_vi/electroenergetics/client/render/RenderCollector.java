package com.george_vi.electroenergetics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.List;

/** Geometry extracted while the world is accessible and submitted without retaining game objects. */
public final class RenderCollector {
    public boolean hideBlockOutline;
    public net.minecraft.world.level.CardinalLighting cardinalLighting;
    private final List<Deferred> outlines = new ArrayList<>();

    public void outline(
            PoseStack matrices,
            java.util.function.BiConsumer<PoseStack, SubmitNodeCollector> renderer) {
        PoseStack.Pose copy = new PoseStack.Pose();
        copy.set(matrices.last());
        outlines.add(new Deferred(copy, renderer));
    }

    private final List<Geometry> geometry = new ArrayList<>();
    private final List<Deferred> submissions = new ArrayList<>();

    public void defer(
            PoseStack matrices,
            java.util.function.BiConsumer<PoseStack, SubmitNodeCollector> submission) {
        PoseStack.Pose copy = new PoseStack.Pose();
        copy.set(matrices.last());
        submissions.add(new Deferred(copy, submission));
    }

    public void text(
            PoseStack matrices,
            net.minecraft.network.chat.Component text,
            float x,
            float y,
            int color,
            int light) {
        var visualText = text.getVisualOrderText();
        defer(
                matrices,
                (pose, queue) ->
                        queue.submitText(
                                pose,
                                x,
                                y,
                                visualText,
                                true,
                                net.minecraft.client.gui.Font.DisplayMode.NORMAL,
                                light,
                                color,
                                0,
                                0));
    }

    public Target getBuffer(RenderType type) {
        return new Target(this, type);
    }

    void add(SuperByteBufferRenderState state, RenderType type, PoseStack.Pose pose) {
        PoseStack.Pose copy = new PoseStack.Pose();
        copy.set(pose);
        geometry.add(new Geometry(state, type, copy));
    }

    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        for (Geometry part : geometry) {
            matrices.pushPose();
            com.zurrtum.create.client.catnip.render.SuperByteBuffer.mul(matrices.last(), part.pose);
            part.state.submit(part.type, matrices, queue);
            matrices.popPose();
        }
        for (Deferred part : submissions) {
            matrices.pushPose();
            com.zurrtum.create.client.catnip.render.SuperByteBuffer.mul(matrices.last(), part.pose);
            part.submission.accept(matrices, queue);
            matrices.popPose();
        }
    }

    public void submitOutlines(PoseStack matrices, SubmitNodeCollector queue) {
        for (Deferred part : outlines) {
            matrices.pushPose();
            com.zurrtum.create.client.catnip.render.SuperByteBuffer.mul(matrices.last(), part.pose);
            part.submission.accept(matrices, queue);
            matrices.popPose();
        }
    }

    public boolean isEmpty() {
        return geometry.isEmpty();
    }

    public record Target(RenderCollector collector, RenderType type) {}

    private record Geometry(
            SuperByteBufferRenderState state, RenderType type, PoseStack.Pose pose) {}

    private record Deferred(
            PoseStack.Pose pose,
            java.util.function.BiConsumer<PoseStack, SubmitNodeCollector> submission) {}
}
