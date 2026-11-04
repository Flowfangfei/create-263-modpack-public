package com.railwayteam.railways.internal.compat.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.lib.transform.Transform;
import net.createmod.catnip.api.client.render.SpriteShiftEntry;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import java.util.function.Supplier;

/** Independent command state over Catnip geometry; shared geometry is reset only while drawing. */
public final class SuperByteBuffer implements Transform<SuperByteBuffer> {
    private final Supplier<net.createmod.catnip.api.client.render.SuperByteBuffer> geometry;
    private final PoseStack transforms = new PoseStack();
    private int light, overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, color = -1;
    private boolean diffuse = true;
    private BlockAndTintGetter level;
    private SpriteShiftEntry shift;
    private float uShift, vShift;
    public SuperByteBuffer(Supplier<net.createmod.catnip.api.client.render.SuperByteBuffer> geometry) { this.geometry=geometry; }
    private SuperByteBuffer snapshot() {
        var copy=new SuperByteBuffer(geometry);
        copy.transforms.last().pose().set(transforms.last().pose());
        copy.transforms.last().normal().set(transforms.last().normal());
        copy.light=light; copy.overlay=overlay; copy.color=color; copy.diffuse=diffuse;
        copy.level=level; copy.shift=shift; copy.uShift=uShift; copy.vShift=vShift;
        return copy;
    }
    @Override public SuperByteBuffer translate(float x,float y,float z) { transforms.translate(x,y,z);return this; }
    @Override public SuperByteBuffer rotate(Quaternionfc rotation) { transforms.rotate(new Quaternionf(rotation));return this; }
    @Override public SuperByteBuffer scale(float x,float y,float z) { transforms.scale(x,y,z);return this; }
    @Override public SuperByteBuffer mulPose(Matrix4fc pose) { transforms.last().pose().mul(pose);return this; }
    @Override public SuperByteBuffer mulNormal(Matrix3fc normal) { transforms.last().normal().mul(normal);return this; }
    public SuperByteBuffer light(int value) { light=value;return this; }
    public SuperByteBuffer overlay(int value) { overlay=value;return this; }
    public SuperByteBuffer color(int value) { color=value|0xFF000000;return this; }
    public SuperByteBuffer color(int r,int g,int b,int a) { color=(a<<24)|(r<<16)|(g<<8)|b;return this; }
    public SuperByteBuffer disableDiffuse() { diffuse=false;return this; }
    public SuperByteBuffer cardinalLighting(net.minecraft.world.level.Level value) { level=value instanceof BlockAndTintGetter getter ? getter : null;return this; }
    public SuperByteBuffer useLevelLight(BlockAndTintGetter value) { level=value;return this; }
    public SuperByteBuffer shiftUV(SpriteShiftEntry value) { shift=value;return this; }
    public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry value,float v) { return shiftUVScrolling(value,0,v); }
    public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry value,float u,float v) { shift=value;uShift=u;vShift=v;return this; }
    public SuperByteBufferRenderState extractRenderState() { return new SuperByteBufferRenderState(snapshot()); }
    public void submit(RenderType type, PoseStack poses, SubmitNodeCollector collector) { extractRenderState().submit(type,poses,collector); }
    public void submit(RenderType type, PoseStack poses, OrderedSubmitNodeCollector collector) { extractRenderState().submit(type,poses,collector); }
    public void renderInto(PoseStack poses, VertexConsumer consumer) {
        var raw=geometry.get(); raw.reset();
        raw.light(light).overlay(overlay).color(color);
        if (!diffuse) raw.disableDiffuse();
        if (level!=null) raw.useLevelLight(level);
        if (shift!=null) raw.shiftUVScrolling(shift,uShift,vShift);
        poses.pushPose();
        poses.last().pose().mul(transforms.last().pose());
        poses.last().normal().mul(transforms.last().normal());
        var complete = new CompleteVertexConsumer(consumer,color,overlay);
        try { raw.renderInto(poses,complete); complete.finish(); } finally { poses.popPose();raw.reset(); }
    }
}
