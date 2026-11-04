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
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;

@Mixin(targets="net.createmod.catnip.api.client.outliner.LineOutline")
public class LineOutlineMixin {
    @Shadow @Final protected Vector3d start;
    @Shadow @Final protected Vector3d end;
    // Wrap the inherited render entry so EndChasingLineOutline's override is also projected.
    @WrapMethod(method="render")
    private void sable$plotOutline(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera,
            float partialTicks, Operation<Void> original) {
        ClientSubLevel sub=SubLevelOutlinePose.find(new Vec3(start.x,start.y,start.z));
        if (sub==null) sub=SubLevelOutlinePose.find(new Vec3(end.x,end.y,end.z));
        if (sub==null) { original.call(ms,buffer,camera,partialTicks); return; }
        SubLevelOutlinePose.runProjected(ms,sub,camera,
            local -> original.call(ms,buffer,local,partialTicks));
    }
}
