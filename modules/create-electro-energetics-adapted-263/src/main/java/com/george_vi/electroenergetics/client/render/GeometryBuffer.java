package com.george_vi.electroenergetics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.transform.Transform;

import net.minecraft.world.level.Level;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

/** Fluent model transforms whose final operation captures an immutable render state. */
public final class GeometryBuffer implements Transform<GeometryBuffer> {
    private final SuperByteBuffer buffer;

    public GeometryBuffer(SuperByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public GeometryBuffer translate(float x, float y, float z) {
        buffer.translate(x, y, z);
        return this;
    }

    @Override
    public GeometryBuffer scale(float x, float y, float z) {
        buffer.scale(x, y, z);
        return this;
    }

    @Override
    public GeometryBuffer rotate(Quaternionfc q) {
        buffer.rotate(q);
        return this;
    }

    @Override
    public GeometryBuffer transform(PoseStack.Pose pose) {
        buffer.transform(pose);
        return this;
    }

    @Override
    public GeometryBuffer mulPose(Matrix4fc pose) {
        buffer.mulPose(pose);
        return this;
    }

    @Override
    public GeometryBuffer mulNormal(Matrix3fc normal) {
        buffer.mulNormal(normal);
        return this;
    }

    public GeometryBuffer color(int color) {
        buffer.color((color & 0xff000000) == 0 ? color | 0xff000000 : color);
        return this;
    }

    public GeometryBuffer color(int r, int g, int b, int a) {
        buffer.color(r, g, b, a);
        return this;
    }

    public GeometryBuffer color(Color color) {
        buffer.color(color);
        return this;
    }

    public GeometryBuffer light(int light) {
        buffer.light(light);
        return this;
    }

    public GeometryBuffer useLevelLight(
            net.minecraft.world.level.BlockAndLightGetter level, org.joml.Matrix4f transform) {
        buffer.useLevelLight(level, transform);
        return this;
    }

    public GeometryBuffer overlay(int overlay) {
        buffer.overlay(overlay);
        return this;
    }

    public GeometryBuffer disableDiffuse() {
        buffer.disableDiffuse();
        return this;
    }

    public GeometryBuffer cardinalLighting(Level level) {
        buffer.cardinalLighting(level);
        return this;
    }

    public void renderInto(PoseStack matrices, RenderCollector.Target target) {
        if (target.collector().cardinalLighting != null)
            buffer.cardinalLighting(target.collector().cardinalLighting);
        target.collector().add(buffer.extractRenderState(), target.type(), matrices.last());
    }
}
