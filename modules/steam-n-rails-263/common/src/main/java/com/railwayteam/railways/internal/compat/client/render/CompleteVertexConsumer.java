package com.railwayteam.railways.internal.compat.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;

/** Completes Catnip's block-only stream for 26.3 feature pipelines that also require entity attributes. */
public final class CompleteVertexConsumer implements VertexConsumer {
    private final VertexConsumer target;
    private final int tint;
    private final int overlay;
    private final float[][] positions = new float[4][3];
    private final float[][] uv = new float[4][2];
    private final float[][] extraUv = new float[4][2];
    private final float[] lineWidths = {1,1,1,1};
    private final int[] colors = new int[4];
    private final int[] lights = new int[4];
    private final int[] overlays = new int[4];
    private final float[][] normals = new float[4][3];
    private final boolean[] suppliedNormals = new boolean[4];
    private final Vector3f edge = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private int index = -1;

    public CompleteVertexConsumer(VertexConsumer target, int tint, int overlay) {
        this.target=target;this.tint=tint;this.overlay=overlay;
    }
    @Override public VertexConsumer addVertex(float x,float y,float z) {
        if(index==3) flushQuad();
        ++index;positions[index][0]=x;positions[index][1]=y;positions[index][2]=z;
        colors[index]=-1;lights[index]=0;overlays[index]=overlay;suppliedNormals[index]=false;extraUv[index][0]=extraUv[index][1]=0;lineWidths[index]=1;
        return this;
    }
    @Override public VertexConsumer setColor(int r,int g,int b,int a) { return setColor((a<<24)|(r<<16)|(g<<8)|b); }
    @Override public VertexConsumer setColor(int argb) { colors[index]=ARGB.multiply(argb,tint);return this; }
    @Override public VertexConsumer setUv(float u,float v) { uv[index][0]=u;uv[index][1]=v;return this; }
    @Override public VertexConsumer setUv1(int u,int v) { overlays[index]=(u&65535)|((v&65535)<<16);return this; }
    @Override public VertexConsumer setUv2(int u,int v) { lights[index]=(u&65535)|((v&65535)<<16);return this; }
    @Override public VertexConsumer setUv3(float u,float v) { extraUv[index][0]=u;extraUv[index][1]=v;return this; }
    @Override public VertexConsumer setNormal(float x,float y,float z) { suppliedNormals[index]=true;normals[index][0]=x;normals[index][1]=y;normals[index][2]=z;return this; }
    @Override public VertexConsumer setLineWidth(float width) { lineWidths[index]=width;return this; }
    public void finish() {
        if(index==3) flushQuad();
        else if(index!=-1) throw new IllegalStateException("Incomplete Catnip quad: "+(index+1)+" vertices");
    }
    private void flushQuad() {
        normal.set(positions[1][0]-positions[0][0],positions[1][1]-positions[0][1],positions[1][2]-positions[0][2]);
        edge.set(positions[2][0]-positions[0][0],positions[2][1]-positions[0][1],positions[2][2]-positions[0][2]);
        normal.cross(edge);
        if(normal.lengthSquared()>1e-12f) normal.normalize(); else normal.set(0,1,0);
        for(int i=0;i<4;i++) {
            target.addVertex(positions[i][0],positions[i][1],positions[i][2],colors[i],uv[i][0],uv[i][1],
                overlays[i],lights[i],suppliedNormals[i]?normals[i][0]:normal.x,
                suppliedNormals[i]?normals[i][1]:normal.y,suppliedNormals[i]?normals[i][2]:normal.z);
            target.setUv3(extraUv[i][0],extraUv[i][1]);
            target.setLineWidth(lineWidths[i]);
        }
        index=-1;
    }
}
