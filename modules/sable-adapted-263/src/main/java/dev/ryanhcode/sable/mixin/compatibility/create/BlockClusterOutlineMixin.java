package dev.ryanhcode.sable.mixin.compatibility.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinhelpers.compatibility.create.SubLevelOutlinePose;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.createmod.catnip.api.client.render.SuperRenderTypeBuffer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets="net.createmod.catnip.api.client.outliner.BlockClusterOutline")
public class BlockClusterOutlineMixin {
    @WrapOperation(method={"renderFaces","renderEdges"},
        at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V"))
    private void sable$plotAnchor(PoseStack ms,double x,double y,double z,Operation<Void> original,
            @Local(argsOnly=true) Vec3 camera) {
        Vec3 anchor=new Vec3(x+camera.x,y+camera.y,z+camera.z);
        ClientSubLevel sub=SubLevelOutlinePose.find(anchor);
        if(sub==null){original.call(ms,x,y,z);return;}
        var pose=sub.renderPose();
        SubLevelOutlinePose.apply(pose,ms);
        Vec3 local=SubLevelOutlinePose.plotCamera(pose,camera);
        original.call(ms,anchor.x-local.x,anchor.y-local.y,anchor.z-local.z);
    }
}
