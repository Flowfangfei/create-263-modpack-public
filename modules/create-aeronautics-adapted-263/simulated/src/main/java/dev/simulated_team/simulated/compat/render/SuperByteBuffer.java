package dev.simulated_team.simulated.compat.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.api.client.render.SpriteShiftEntry;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Keeps every submitted draw's transforms and material state independent of Catnip's mutable cache. */
public final class SuperByteBuffer implements dev.engine_room.flywheel.lib.transform.Affine<SuperByteBuffer>, net.createmod.catnip.api.client.render.SuperByteBuffer {
    private final Supplier<net.createmod.catnip.api.client.render.SuperByteBuffer> geometry;
    private PoseStack transforms=new PoseStack();
    private final List<Consumer<net.createmod.catnip.api.client.render.SuperByteBuffer>> actions=new ArrayList<>();
    public SuperByteBuffer(Supplier<net.createmod.catnip.api.client.render.SuperByteBuffer> geometry){this.geometry=geometry;}
    public static SuperByteBuffer wrap(net.createmod.catnip.api.client.render.SuperByteBuffer buffer){return buffer instanceof SuperByteBuffer own?own:new SuperByteBuffer(()->buffer);}
    @Override public PoseStack getTransforms(){return transforms;}
    @Override public boolean isEmpty(){return geometry.get().isEmpty();}
    @Override public SuperByteBuffer reset(){transforms=new PoseStack();actions.clear();return this;}
    @Override public SuperByteBuffer color(int color){actions.add(b->b.color(color));return this;}
    @Override public SuperByteBuffer color(int r,int g,int b,int a){actions.add(buf->buf.color(r,g,b,a));return this;}
    @Override public SuperByteBuffer color(Color color){return color(color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha());}
    @Override public SuperByteBuffer disableDiffuse(){actions.add(b->b.disableDiffuse());return this;}
    @Override public SuperByteBuffer shiftUV(SpriteShiftEntry entry){actions.add(b->b.shiftUV(entry));return this;}
    @Override public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry,float u,float v){actions.add(b->b.shiftUVScrolling(entry,u,v));return this;}
    @Override public SuperByteBuffer shiftUVScrolling(SpriteShiftEntry entry,float v){return shiftUVScrolling(entry,0,v);}
    @Override public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry,float u,float v,int size){actions.add(b->b.shiftUVtoSheet(entry,u,v,size));return this;}
    @Override public SuperByteBuffer overlay(int value){actions.add(b->b.overlay(value));return this;}
    @Override public SuperByteBuffer light(int value){actions.add(b->b.light(value));return this;}
    @Override public SuperByteBuffer useLevelLight(BlockAndTintGetter level){actions.add(b->b.useLevelLight(level));return this;}
    @Override public SuperByteBuffer useLevelLight(BlockAndTintGetter level,Matrix4f transform){var copy=new Matrix4f(transform);actions.add(b->b.useLevelLight(level,copy));return this;}
    public SuperByteBuffer translate(float x,float y,float z){transforms.translate(x,y,z);return this;}
    public SuperByteBuffer translate(double x,double y,double z){transforms.translate(x,y,z);return this;}
    public SuperByteBuffer translate(Vec3 offset){return translate(offset.x,offset.y,offset.z);}
    public SuperByteBuffer translate(net.minecraft.core.Vec3i offset){return translate(offset.getX(),offset.getY(),offset.getZ());}
    public SuperByteBuffer translate(org.joml.Vector3fc offset){return translate(offset.x(),offset.y(),offset.z());}
    public SuperByteBuffer rotateCentered(Quaternionfc rotation){return center().rotate(rotation).uncenter();}
    public SuperByteBuffer rotateXCentered(float angle){return center().rotateX(angle).uncenter();}
    public SuperByteBuffer rotateYCentered(float angle){return center().rotateY(angle).uncenter();}
    public SuperByteBuffer rotateZCentered(float angle){return center().rotateZ(angle).uncenter();}
    public SuperByteBuffer rotateXCenteredDegrees(float angle){return rotateXCentered((float)Math.toRadians(angle));}
    public SuperByteBuffer rotateYCenteredDegrees(float angle){return rotateYCentered((float)Math.toRadians(angle));}
    public SuperByteBuffer rotateZCenteredDegrees(float angle){return rotateZCentered((float)Math.toRadians(angle));}
    public SuperByteBuffer scale(float scale){return scale(scale,scale,scale);}
    public SuperByteBuffer scale(float x,float y,float z){transforms.scale(x,y,z);return this;}
    public SuperByteBuffer center(){return translate(.5,.5,.5);}
    public SuperByteBuffer uncenter(){return translate(-.5,-.5,-.5);}
    public SuperByteBuffer rotate(float angle,Direction direction){transforms.rotate(new Quaternionf().rotationAxis(angle,direction.getStepX(),direction.getStepY(),direction.getStepZ()));return this;}
    public SuperByteBuffer rotateCentered(float angle,Direction direction){return center().rotate(angle,direction).uncenter();}
    public SuperByteBuffer rotateToFace(Direction face){return transform(net.createmod.catnip.api.client.render.CachedBuffers.rotateToFace(face).get());}
    public SuperByteBuffer rotateToFaceVertical(Direction face){return transform(net.createmod.catnip.api.client.render.CachedBuffers.rotateToFaceVertical(face).get());}
    public SuperByteBuffer rotateX(float angle){return rotate(angle,Direction.EAST);}
    public SuperByteBuffer rotateY(float angle){return rotate(angle,Direction.UP);}
    public SuperByteBuffer rotateZ(float angle){return rotate(angle,Direction.SOUTH);}
    public SuperByteBuffer rotateXDegrees(float angle){return rotateX((float)Math.toRadians(angle));}
    public SuperByteBuffer rotateYDegrees(float angle){return rotateY((float)Math.toRadians(angle));}
    public SuperByteBuffer rotateZDegrees(float angle){return rotateZ((float)Math.toRadians(angle));}
    public SuperByteBuffer rotate(Quaternionfc rotation){transforms.rotate(rotation);return this;}
    public SuperByteBuffer transform(PoseStack pose){transforms.mulPose(pose.last().pose());return this;}
    public SuperByteBuffer mulPose(Matrix4fc matrix){return transform(matrix);}
    public SuperByteBuffer transform(Matrix4fc matrix){transforms.mulPose(new Matrix4f(matrix));return this;}
    private net.createmod.catnip.api.client.render.SuperByteBuffer prepare(List<Consumer<net.createmod.catnip.api.client.render.SuperByteBuffer>> settings){
        var buffer=geometry.get();buffer.reset();settings.forEach(setting->setting.accept(buffer));return buffer;
    }
    @Override public void renderInto(PoseStack pose,VertexConsumer consumer){
        pose.pushPose();pose.mulPose(transforms.last().pose());prepare(actions).renderInto(pose,consumer);pose.popPose();
    }
    public void submit(PoseStack pose,SubmitNodeCollector collector){submit(RenderTypes.cutoutMovingBlock(),pose,collector);}
    public void submit(RenderType type,PoseStack pose,SubmitNodeCollector collector){
        var state=List.copyOf(actions);PoseStack snapshot=new PoseStack();snapshot.mulPose(pose.last().pose());snapshot.mulPose(transforms.last().pose());
        collector.submitCustomGeometry(snapshot,type,(matrix,consumer)->{PoseStack draw=new PoseStack();draw.mulPose(matrix.pose());prepare(state).renderInto(draw,consumer);});
    }
}
