package dev.simulated_team.simulated.mixin.ponder;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderer;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderState;
import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PonderSceneRenderer.class)
public class PonderUIMixin {
 @Inject(method="renderScene",at=@At(value="INVOKE",target="Lnet/createmod/ponder/api/client/scene/PonderScene;renderScene(Lnet/createmod/catnip/api/client/render/SuperRenderTypeBuffer;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
 private void simulated$shadow(PonderSceneRenderState state,PoseStack pose,SubmitNodeCollector collector,CallbackInfo ci){
    var scene=state.scene();if(scene.shouldHidePlatformShadow())return;
    var extension=(PonderSceneExtension)scene;float fade=extension.simulated$getBasePlateAnimationTimer(state.partialTicks());if(fade<=0)return;
    var offset=extension.simulated$getShadowOffset(state.partialTicks());var bounds=scene.getBounds();
    int color=((int)(0x66*Math.min(fade,1)))<<24;
    float x0=(float)(bounds.minX()+offset.x),x1=(float)(bounds.maxX()+1+offset.x),z0=(float)(bounds.minZ()+offset.z),z1=(float)(bounds.maxZ()+1+offset.z),y=(float)(bounds.minY()-.01+offset.y);
    collector.submitCustomGeometry(pose,net.minecraft.client.renderer.rendertype.RenderTypes.debugQuads(),(matrix,consumer)->{
        consumer.addVertex(matrix.pose(),x0,y,z0).setColor(color);consumer.addVertex(matrix.pose(),x0,y,z1).setColor(color);consumer.addVertex(matrix.pose(),x1,y,z1).setColor(color);consumer.addVertex(matrix.pose(),x1,y,z0).setColor(color);
    });
 }
}
