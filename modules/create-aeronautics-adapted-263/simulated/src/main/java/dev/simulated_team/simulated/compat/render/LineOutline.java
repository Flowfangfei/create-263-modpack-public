package dev.simulated_team.simulated.compat.render;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector4f;
public class LineOutline extends net.createmod.catnip.api.client.outliner.LineOutline {
    @Override public LineOutline set(Vec3 start,Vec3 end){super.set(start,end);return this;}
    public void submit(net.minecraft.client.Minecraft minecraft,PoseStack pose,SubmitNodeCollector collector,Vec3 camera,float partialTick){
        float width=params.getLineWidth();if(width==0)return;
        var from=new Vector3d(start);var to=new Vector3d(end);var color=new Vector4f();params.loadColor(color);
        var accessor=(dev.simulated_team.simulated.mixin.accessor.OutlineParamsAccessor)params;
        int light=accessor.simulated$lightmap();boolean noNormals=accessor.simulated$disableLineNormals();
        collector.submitCustomGeometry(pose,net.createmod.catnip.api.client.render.PonderRenderTypes.outlineSolid(),(matrix,consumer)->{
            PoseStack draw=new PoseStack();draw.mulPose(matrix.pose());bufferCuboidLine(draw,consumer,camera,from,to,width,color,light,noNormals);
        });
    }
}
